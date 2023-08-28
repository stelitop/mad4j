package stelitop.mad4j.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.User;
import lombok.Builder;
import lombok.ToString;
import stelitop.mad4j.utils.ActionResult;
import stelitop.mad4j.components.ComponentInteraction;
import stelitop.mad4j.DiscordEventsComponent;
import stelitop.mad4j.InteractionEvent;
import stelitop.mad4j.convenience.EventUser;
import stelitop.mad4j.convenience.EventUserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class ComponentEventListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Loaded data for the classes that can contain the implementation.
     */
    private Map<Class<? extends ComponentInteractionEvent>, List<ImplementationEntry>> methods;

    @Builder
    @ToString
    private static class ImplementationEntry {
        Object bean;
        String regex;
        Method method;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        loadBeans();
        client.on(ButtonInteractionEvent.class, this::mapEvent).subscribe();
        client.on(SelectMenuInteractionEvent.class, this::mapEvent).subscribe();
        client.on(ModalSubmitInteractionEvent.class, this::mapEvent).subscribe();
    }

    /**
     * Loads the beans annotated with {@link DiscordEventsComponent} and precomputes
     * for all methods annotated with {@link ComponentInteraction} all the necessary
     * information so that it can be cached for future use.
     */
    private void loadBeans() {
        this.methods = new HashMap<>();
        this.methods.put(ButtonInteractionEvent.class, new ArrayList<>());
        this.methods.put(SelectMenuInteractionEvent.class, new ArrayList<>());
        this.methods.put(ModalSubmitInteractionEvent.class, new ArrayList<>());

        Collection<Object> beans = applicationContext.getBeansWithAnnotation(DiscordEventsComponent.class).values();
        for (var bean : beans) {
            List<Method> methods = Arrays.stream(bean.getClass().getMethods())
                    .filter(x -> x.isAnnotationPresent(ComponentInteraction.class))
                    .toList();

            for (var method : methods) {
                ComponentInteraction annotation = method.getAnnotation(ComponentInteraction.class);
                this.methods.get(annotation.event()).add(ImplementationEntry.builder()
                        .bean(bean)
                        .method(method)
                        .regex(annotation.regex())
                        .build());
            }
        }
    }

    /**
     * Attempts to map an event to a loaded {@link ComponentInteraction} method.
     *
     * @param event The event to be mapped.
     * @return A reply to the event if it's successfully mapped and successfully
     *     executed afterward. If unsuccessful, an empty mono is returned and an
     *     error is logged.
     */
    private Mono<Void> mapEvent(ComponentInteractionEvent event) {
        List<ImplementationEntry> possibleEntries = methods.getOrDefault(event.getClass(), null);
        if (possibleEntries == null) {
            LOGGER.error("Event type " + event.getClass().getName() + " not supported!");
            return Mono.empty();
        }
        String eventId = event.getCustomId();
        List<ImplementationEntry> matches = possibleEntries.stream()
                .filter(x -> eventId.matches(x.regex))
                .toList();
        if (matches.isEmpty()) {
            LOGGER.error("No declared interaction matched id \"" + eventId + "\" of event type " + event.getClass() + "!");
            return Mono.empty();
        }
        if (matches.size() > 1) {
            String errorMsg = matches.stream()
                    .map(x -> "Method \"" + x.method.getName() + "\" in class \"" + x.bean.getClass().getName() + "\"!")
                    .collect(Collectors.joining("\n"));
            LOGGER.error("Multiple interactions match the id \"" + eventId + "\"!\n" + errorMsg);
            return Mono.empty();
        }

        return executeEvent(event, matches.get(0));
    }

    /**
     * Attempts to execute an event with a specific implementation.
     *
     * @param event The event to be executed.
     * @param imp The implementation data to use for execution.
     * @return The reply to the event if it's successfully executed. If it's not,
     *     an empty mono is returned instead and an error is logged.
     */
    private Mono<Void> executeEvent(ComponentInteractionEvent event, ImplementationEntry imp) {
        Parameter[] parameters = imp.method.getParameters();
        Object[] args = new Object[parameters.length];
        String errorStart = "Method \"" + imp.method.getName() + "\" in class \"" + imp.bean.getClass().getName() + "\"";

        for (int i = 0; i < parameters.length; i++) {
            ActionResult<Object> mapParamAR = mapParam(event, parameters[i]);
            if (mapParamAR.hasFailed()) {
                LOGGER.error(errorStart + " had a problem injecting parameter at position " + i + ". Message: " + mapParamAR.errorMessage());
                return Mono.empty();
            }
            args[i] = mapParamAR.getResponse();
        }


        if (!imp.method.getReturnType().equals(Mono.class)) {
            LOGGER.error(errorStart + "'s result could not be cast to Mono<Void>. Check method signature.");
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        }

        try {
            Object result = imp.method.invoke(imp.bean, args);
            if (Mono.class.isAssignableFrom(result.getClass())) {
                return ((Mono<?>) result).cast(Void.class);
            }
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(errorStart + " had a problem during invoking!");
            throw new RuntimeException(e);
        } catch (ClassCastException e) {
            LOGGER.error(errorStart + "'s result could not be cast to Mono<Void>. Check method signature.");
            return event.reply("Could not cast result of slash command.")
                    .withEphemeral(true);
        }
    }

    /**
     * Maps a single parameter from the method signature to the wanted value,
     * depending on its annotations.
     *
     * @param event The event for the method.
     * @param param The parameter data.
     * @return The value to inject for the parameter. In case it doesn't match
     *     anything, null is returned.
     */
    private @NotNull ActionResult<@Nullable Object> mapParam(@NotNull ComponentInteractionEvent event, @NotNull Parameter param) {
        if (param.isAnnotationPresent(InteractionEvent.class)) {
            if (!param.getType().isInstance(event)) {
                return ActionResult.fail("The @InteractionEvent type is not compatible!");
            }
            return ActionResult.success(event);
        }
        if (param.isAnnotationPresent(EventUser.class)) {
            if (param.getType() != User.class) {
                return ActionResult.fail("The @EventUser type is not a User type!");
            }
            return ActionResult.success(event.getInteraction().getUser());
        }
        if (param.isAnnotationPresent(EventUserId.class)) {
            if (param.getType() != Long.class && param.getType() != long.class) {
                return ActionResult.fail("The @EventUserId type is not a long type!");
            }
            return ActionResult.success(event.getInteraction().getUser().getId().asLong());
        }
        return ActionResult.success(null);
    }

//    /**
//     * <p>Redirects an event to use a different custom id, remapping it from the start.</p>
//     *
//     * <p>A potential use for this is buttons that redirect to a different menu that can
//     * be accessed </p>
//     *
//     * @param event The event to redirect.
//     * @param newCustomId The new custom id under which to redirect the event.
//     * @return
//     */
//    public Mono<Void> redirectEvent(ComponentInteractionEvent event, String newCustomId) {
//        event.getC
//        return mapEvent(event, newCustomId);
//    }
}
