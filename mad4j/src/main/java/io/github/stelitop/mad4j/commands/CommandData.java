package io.github.stelitop.mad4j.commands;

import discord4j.core.GatewayDiscordClient;
import io.github.stelitop.mad4j.DiscordEventsComponent;
import lombok.Builder;
import lombok.Getter;
import io.github.stelitop.mad4j.listeners.CommandOptionAutocompleteListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Order(0)
public class CommandData implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final GatewayDiscordClient gatewayDiscordClient;
    private final CommandOptionAutocompleteListener commandOptionAutocompleteListener;
    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final List<Entry> commandsInfo = new ArrayList<>();

    private static final Set<CommandType> DEFAULT_COMMAND_TYPES = Set.of(CommandType.Slash);

    @Builder
    @Getter
    public static class Entry {
        private String name;
        private String description;
        private Set<CommandType> types;
        private Object bean;
        private Method method;
    }

    @Autowired
    public CommandData(
            GatewayDiscordClient gatewayDiscordClient,
            CommandOptionAutocompleteListener commandOptionAutocompleteListener,
            ApplicationContext applicationContext,
            Environment environment
    ) {
        this.gatewayDiscordClient = gatewayDiscordClient;
        this.commandOptionAutocompleteListener = commandOptionAutocompleteListener;
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        Collection<Object> commandBeans = applicationContext.getBeansWithAnnotation(DiscordEventsComponent.class).values();

        commandsInfo.clear();
        for (var bean : commandBeans) {
            for (var method : bean.getClass().getMethods()) {
                Entry data = getCommandData(bean, method);
                if (data == null) continue;
                commandsInfo.add(data);
            }
        }
        // TODO: Verify no overlap
    }

    private Entry getCommandData(Object bean, Method method) {
        if (method.isAnnotationPresent(Command.class)) {
            Command c = method.getAnnotation(Command.class);
            return Entry.builder()
                    .name(c.name().toLowerCase())
                    .description(c.description())
                    .types(getCommandTypes(c.types()))
                    .bean(bean)
                    .method(method)
                    .build();
        } else if (method.isAnnotationPresent(SlashCommand.class)) {
            SlashCommand sc = method.getAnnotation(SlashCommand.class);
            return Entry.builder()
                    .name(sc.name().toLowerCase())
                    .description(sc.description())
                    .types(Set.of(CommandType.Slash))
                    .bean(bean)
                    .method(method)
                    .build();
        }
        return null;
    }

    private Set<CommandType> getCommandTypes(CommandType[] types) {
        if (types.length == 0) return DEFAULT_COMMAND_TYPES;
        else return Arrays.stream(types).collect(Collectors.toSet());
    }

    public @Nullable Entry get(String commandName, CommandType type) {
        String nameLower = commandName.toLowerCase();
        return commandsInfo.stream()
                .filter(x -> x.name.toLowerCase().equals(nameLower) && x.types.contains(type))
                .findFirst()
                .orElse(null);
    }
    public List<Entry> getFromType(CommandType type) {
        return commandsInfo.stream()
                .filter(x -> x.types.contains(type))
                .toList();
    }
}
