package net.stelitop.mad4j.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.stelitop.mad4j.requirements.CommandRequirementExecutor;
import net.stelitop.mad4j.utils.ActionResult;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.CommandParam;
import net.stelitop.mad4j.InteractionEvent;
import net.stelitop.mad4j.commands.SlashCommand;
import net.stelitop.mad4j.convenience.EventUser;
import net.stelitop.mad4j.convenience.EventUserId;
import net.stelitop.mad4j.utils.OptionType;
import net.stelitop.mad4j.requirements.CommandRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private final Map<Class<? extends CommandRequirementExecutor>, CommandRequirementExecutor> possibleRequirements;
    private List<SlashCommandEntry> slashCommands;

    public SlashCommandListener(
            ApplicationContext applicationContext,
            GatewayDiscordClient client,
            List<CommandRequirementExecutor> possibleRequirements
    ) {
        this.applicationContext = applicationContext;
        this.client = client;
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

        Collection<Object> commandBeans = applicationContext.getBeansWithAnnotation(DiscordEventsComponent.class).values();

        slashCommands = new ArrayList<>();
        for (var bean : commandBeans) {
            var slashCommandMethods = Arrays.stream(bean.getClass().getMethods())
                    .filter(x -> x.isAnnotationPresent(SlashCommand.class))
                    .toList();

            for (var method : slashCommandMethods) {
                var newEntry = new SlashCommandEntry();
                SlashCommand slashCommandAnnotation = method.getAnnotation(SlashCommand.class);
                newEntry.method = method;
                newEntry.name = slashCommandAnnotation.name().toLowerCase();
                newEntry.sourceBean = bean;
                slashCommands.add(newEntry);
            }
        }

        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class SlashCommandEntry {
        private Object sourceBean;
        private Method method;
        private String name;
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
    private Mono<Void> handle(ChatInputInteractionEvent event) {
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

        Optional<SlashCommandEntry> command = this.slashCommands.stream()
                .filter(x -> x.name.equalsIgnoreCase(commandName))
                .findFirst();

        if (command.isEmpty()) {
            return event.reply("Could not resolve command '" + commandName + "'.")
                    .withEphemeral(true);
        }
        return invokeSlashCommand(event, optionsMap, command.get());
    }

    private Mono<Void> invokeSlashCommand(
            ChatInputInteractionEvent event,
            Map<String, ApplicationCommandInteractionOption> options,
            SlashCommandEntry command
    ) {

        ActionResult<Void> conditionsResult = verifyCommandConditions(event, command);
        if (conditionsResult.hasFailed()) {
            return event.reply(conditionsResult.errorMessage())
                    .withEphemeral(true);
        }

        Parameter[] parameters = command.method.getParameters();
        List<Object> invocationParams = Arrays.stream(parameters)
                .map(p -> mapCommandParamToMethodParam(event, options, p))
                .toList();

        try {
            Object result = command.method.invoke(command.sourceBean, invocationParams.toArray());
            return (Mono<Void>) result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(command.name + " had a problem during invoking.");
            e.printStackTrace();
            return event.reply("An error occurred invoking the slash command!")
                    .withEphemeral(true);
        } catch (ClassCastException e) {
            LOGGER.error(command.name + "'s result could not be cast to Mono<Void>. Check method signature.");
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        }
    }

    private Object mapCommandParamToMethodParam(
            ChatInputInteractionEvent event,
            Map<String, ApplicationCommandInteractionOption> options,
            Parameter param
    ) {
        if (param.isAnnotationPresent(InteractionEvent.class)) return event;
        if (param.isAnnotationPresent(EventUser.class)) return event.getInteraction().getUser();
        if (param.isAnnotationPresent(EventUserId.class)) return event.getInteraction().getUser().getId().asLong();

        if (param.isAnnotationPresent(CommandParam.class)) {
            CommandParam annotation = param.getAnnotation(CommandParam.class);
            if (!options.containsKey(annotation.name().toLowerCase())) {
                return null;
            }
            ApplicationCommandInteractionOption option = options.get(annotation.name().toLowerCase());
            return getValueFromOption(option);
        }

        return null;
    }

    private Object getValueFromOption(ApplicationCommandInteractionOption option) {
        if (option.getValue().isEmpty()) {
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

    private ActionResult<Void> verifyCommandConditions(ChatInputInteractionEvent event, SlashCommandEntry command) {
        List<CommandRequirement> conditionAnnotations = Arrays.stream(command.method.getAnnotations())
                .map(a -> a.annotationType().getAnnotation(CommandRequirement.class))
                .filter(Objects::nonNull)
                .toList();

        for (var annotation : conditionAnnotations) {
            CommandRequirementExecutor conditionBean = possibleRequirements.get(annotation.implementation());
            if (conditionBean == null) continue;
            ActionResult<Void> result = conditionBean.verify(event);
            if (result.hasFailed()) return result;
        }
        return ActionResult.success();
    }
}
