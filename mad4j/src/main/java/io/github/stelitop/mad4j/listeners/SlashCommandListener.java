package io.github.stelitop.mad4j.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import io.github.stelitop.mad4j.DiscordEventsComponent;
import io.github.stelitop.mad4j.commands.*;
import io.github.stelitop.mad4j.events.AllowedEventResultHandler;
import io.github.stelitop.mad4j.utils.ActionResult;
import io.github.stelitop.mad4j.utils.OptionType;
import io.github.stelitop.mad4j.commands.requirements.CommandRequirementExecutor;
import io.github.stelitop.mad4j.commands.convenience.EventUser;
import io.github.stelitop.mad4j.commands.convenience.EventUserId;
import io.github.stelitop.mad4j.commands.requirements.CommandRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Listener for all slash command events, specifically all {@link ChatInputInteractionEvent}
 * events.</p>
 *
 * <p>When the application loads, all methods annotated with {@link SlashCommand} are loaded
 * into the component. Then, when an event occurs, they are mapped to the corresponding slash
 * command method, the values are mapped to the parameters and the method is invoked from
 * its bean, which must be annotated with {@link DiscordEventsComponent}.</p>
 *
 * <p>The method might be additionally annotated with any {@link CommandRequirement} annotations.
 * These requirements are first checked against and if any of them are unfulfilled, the execution
 * is cancelled and an error message is returned instead.</p>
 */
@Component
public class SlashCommandListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    // dependencies
    private final ApplicationContext applicationContext;
    private final GatewayDiscordClient client;
    private final CommandData commandData;
    private final AllowedEventResultHandler allowedEventResultHandler;

    private final Map<Class<? extends CommandRequirementExecutor>, CommandRequirementExecutor> possibleRequirements;

    @Autowired
    public SlashCommandListener(
            ApplicationContext applicationContext,
            GatewayDiscordClient client,
            CommandData commandData,
            AllowedEventResultHandler allowedEventResultHandler,
            List<CommandRequirementExecutor> possibleRequirements
    ) {
        this.applicationContext = applicationContext;
        this.client = client;
        this.commandData = commandData;
        this.allowedEventResultHandler = allowedEventResultHandler;
        this.possibleRequirements = possibleRequirements.stream()
                .collect(Collectors.toMap(CommandRequirementExecutor::getClass, x -> x));
    }

    /**
     * <p>Method executed when the application starts.</p>
     *
     * <p>Here </p>
     *
     * @param args incoming application arguments
     */
    @Override
    public void run(ApplicationArguments args) {

        Collection<Object> commandBeans = applicationContext
                .getBeansWithAnnotation(DiscordEventsComponent.class)
                .values();
        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    /**
     * <p>Handles the {@link ChatInputInteractionEvent} event.</p>
     *
     * <p>First the full name of the command is extracted from the options. Then we
     * search for a registered bean that contains a method corresponding to the
     * command name. If we find one, we try to map the other command options to
     * its parameters and finally execute it.</p>
     *
     * @param event The event that occurs.
     * @return The mono emitted from the event.
     */
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        StringBuilder commandNameBuilder = new StringBuilder(event.getCommandName().toLowerCase());
        List<ApplicationCommandInteractionOption> options = event.getOptions();
        while (options.size() == 1
                && (options.get(0).getType().getValue() == OptionType.SUB_COMMAND
                || options.get(0).getType().getValue() == OptionType.SUB_COMMAND_GROUP)) {

            commandNameBuilder.append(" ").append(options.get(0).getName().toLowerCase());
            options = options.get(0).getOptions();
        }
        String commandName = commandNameBuilder.toString();

        Map<String, ApplicationCommandInteractionOption> optionsMap = options.stream()
                .collect(Collectors.toMap(x -> x.getName().toLowerCase(), x -> x));

        CommandData.Entry command = commandData.get(commandName, CommandType.Slash);

        if (command == null) {
            return event.reply("Could not resolve command '" + commandName + "'.")
                    .withEphemeral(true);
        }
        return invokeSlashCommand(event, optionsMap, command);
    }

    private Mono<Void> invokeSlashCommand(
            ChatInputInteractionEvent event,
            Map<String, ApplicationCommandInteractionOption> options,
            CommandData.Entry command
    ) {

        ActionResult<Void> conditionsResult = verifyCommandRequirements(event, command);
        if (conditionsResult.hasFailed()) {
            return event.reply(conditionsResult.errorMessage())
                    .withEphemeral(true);
        }

        Parameter[] parameters = command.getMethod().getParameters();
        List<Object> invocationParams = Arrays.stream(parameters)
                .map(p -> mapCommandParamToMethodParam(event, options, p))
                .toList();


        try {
            Object result = command.getMethod().invoke(command.getBean(), invocationParams.toArray());
            var eventResponse = allowedEventResultHandler.handleEventResult(result, event);
            if (eventResponse.isSuccessful()) {
                return eventResponse.getResponse();
            } else {
                // TODO: Handle with an exception
                return event.reply("An error occurred invoking this slash command!")
                        .withEphemeral(true);
            }
//            if (result instanceof EventResponse er) {
//                return er.respond(event);
//            }
//            else if (Mono.class.isAssignableFrom(result.getClass())) {
//                return ((Mono<?>) result).cast(Void.class);
//            }
//            else throw new ClassCastException();

        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(command.getName() + " had a problem during invoking.");
            e.printStackTrace();
            return event.reply("An error occurred invoking this slash command!")
                    .withEphemeral(true);
        } catch (ClassCastException e) {
            LOGGER.error(command.getName() + "'s result could not be cast to any acceptable type. Check method signature.");
            e.printStackTrace();
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        }
    }

    /**
     * <p>Maps data from the d4j API of a slash command to a method parameter of
     * a mad4j command method. This primarily includes the parameters of the command, annotated
     * with {@link CommandParam}, but also includes convenience injections, such as {@link EventUser}
     * abd {@link InteractionEvent}.</p>
     *
     * @param event The d4j slash command event.
     * @param options The
     * @param param Reflection object of the parameter we want to inject in the method invokation
     * @return The value of the command to inject into the parameter, or null if there was no
     *     injectable annotation present or another error occurred.
     */
    private Object mapCommandParamToMethodParam(
            ChatInputInteractionEvent event,
            Map<String, ApplicationCommandInteractionOption> options,
            Parameter param
    ) {
        // Convenience injectors.
        // Inject the interaction event. Does not check if this is the correct event type
        if (param.isAnnotationPresent(InteractionEvent.class)) return event;
        // Inject the User object
        if (param.isAnnotationPresent(EventUser.class)) return event.getInteraction().getUser();
        // Inject the Discord ID of the user
        if (param.isAnnotationPresent(EventUserId.class)) return event.getInteraction().getUser().getId().asLong();

        // Finally, check for actual command parameters.
        if (!param.isAnnotationPresent(CommandParam.class)) return null;
        CommandParam annotation = param.getAnnotation(CommandParam.class);
        if (!options.containsKey(annotation.name().toLowerCase())) {
            if (!param.isAnnotationPresent(DefaultValue.class)) return null;
            DefaultValue dv = param.getAnnotation(DefaultValue.class);

            Class<?> paramClass = param.getType();
            if (paramClass.equals(double.class)) return dv.number();
            else if (paramClass.equals(int.class)) return (int)dv.number();
            else if (paramClass.equals(long.class)) return (long)dv.number();
            else if (paramClass.equals(String.class)) return dv.string();
            else if (paramClass.equals(boolean.class)) return dv.bool();
            else return null;
        }
        ApplicationCommandInteractionOption option = options.get(annotation.name().toLowerCase());
        return getValueFromOption(option, param);
    }

    /**
     * Extracts the value from a d4j slash command option. If there is no value, but there is a
     * default value given, then that is returned.
     *
     * @param option The dj4 slash command interactino option.
     * @param param The reflection parameter corresponding to the mad4j command method.
     * @return The value to inject into the parameter.
     */
    private Object getValueFromOption(ApplicationCommandInteractionOption option, Parameter param) {
        if (option.getValue().isEmpty()) {
            if (param.isAnnotationPresent(DefaultValue.class)) {
                DefaultValue dv = param.getAnnotation(DefaultValue.class);
                return switch (option.getType()) {
                    case NUMBER -> dv.number();
                    case INTEGER -> (long)(dv.number());
                    case STRING -> dv.string();
                    case BOOLEAN -> dv.bool();
                    default -> null;
                };
            }
            return null;
        }
        ApplicationCommandInteractionOptionValue value = option.getValue().get();
        return switch (option.getType()) {
            case BOOLEAN -> value.asBoolean();
            case INTEGER -> value.asLong();
            case STRING -> value.asString();
            case NUMBER -> value.asDouble();
            case CHANNEL -> value.asChannel().block();
            case USER -> value.asUser().block();
            case ROLE -> value.asRole().block();
            default -> null;
        };
    }

    /**
     * Checks that all attached command requirements have been fulfilled. If any of them are not,
     * the first that fails returns their error message.
     *
     * @param event The even that triggered the slash command.
     * @param command The data about the command.
     * @return An action result that succeeds if the condition is fulfilled or fails with an error
     *     message otherwise.
     */
    private ActionResult<Void> verifyCommandRequirements(ChatInputInteractionEvent event, CommandData.Entry command) {
        List<CommandRequirement> requirementAnnotations = Arrays.stream(command.getMethod().getAnnotations())
                .map(a -> a.annotationType().getAnnotation(CommandRequirement.class))
                .filter(Objects::nonNull)
                .toList();

        for (var annotation : requirementAnnotations) {
            CommandRequirementExecutor conditionBean = possibleRequirements.get(annotation.implementation());
            if (conditionBean == null) continue;
            ActionResult<Void> result = conditionBean.verify(event);
            if (result.hasFailed()) return result;
        }
        return ActionResult.success();
    }
}
