package io.github.stelitop.mad4j.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.Id;
import io.github.stelitop.mad4j.commands.CommandParam;
import io.github.stelitop.mad4j.commands.CommandType;
import io.github.stelitop.mad4j.commands.convenience.EventUser;
import io.github.stelitop.mad4j.commands.CommandData;
import io.github.stelitop.mad4j.commands.convenience.EventUserId;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MessageListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final ApplicationContext applicationContext;
    private final GatewayDiscordClient client;
    private final CommandData commandData;

    @Autowired
    public MessageListener(
            ApplicationContext applicationContext,
            GatewayDiscordClient client,
            CommandData commandData
    ) {
        this.applicationContext = applicationContext;
        this.client = client;
        this.commandData = commandData;
    }

    @Override
    public void run(ApplicationArguments args) {
        client.on(MessageCreateEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(MessageCreateEvent event) {
        String content = event.getMessage().getContent();
        // TODO: Replace with proper prefix check that is based on Guild ID or a default one in DMs
        if (!content.startsWith("!")) return Mono.empty();

        String contentNoPrefix = content.substring("!".length());
        List<String> parts = splitMessage(contentNoPrefix);
        if (parts.size() == 0) return Mono.empty();
        String commandName = parts.get(0);
        int commandCutoff = 1;
        CommandData.Entry command = commandData.get(commandName, CommandType.Text);
        while (commandCutoff < parts.size() && command == null) {
            commandName += " " + parts.get(commandCutoff);
            commandCutoff++;
            command = commandData.get(commandName, CommandType.Text);
        }
        if (command == null) return Mono.empty();

//        List<Object> commandParams = parts
//                .subList(commandCutoff, parts.size())
//                .stream()
//                .map(s -> parseParam(s, event))
//                .toList();

        List<Object> methodParams = getOrderedMethodParams(event, parts, command.getMethod());
        try {
            command.getMethod().invoke(command.getBean(), methodParams);
        } catch (IllegalAccessException | InvocationTargetException e) {
            //throw new RuntimeException(e);
            return Mono.empty();
        }

        return Mono.empty();
    }

    /**
     * Splits a command message into separate parts separated by spaces. Parts surrounded
     * by quotation marks are counted as a single string.
     *
     * @param rawContent The message received by the bot, with the command prefix excluded
     * @return List of parts from the emssage.
     */
    public List<String> splitMessage(String rawContent) {
        String[] parts = rawContent.split(" ");
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isBlank()) continue;
            if (parts[i].startsWith("\"")) {
                List<String> segments = new ArrayList<>();
                while (i < parts.length) {
                    segments.add(parts[i]);
                    if (parts[i].endsWith("\"") && !parts[i].endsWith("\\\"")) break;
                    i++;
                }
                String part = String.join(" ", segments);
                System.out.println(part);
                if (part.endsWith("\"") && !part.endsWith("\\\"")) part = part.substring(1, part.length() - 1);
                ret.add(part);
            }
            else {
                ret.add(parts[i]);
            }
        }

        return ret;
    }

    /**
     * Parses a parameter from how it was input as a String into a java object
     * @param s
     * @return
     */
    private Object parseParam(String s, MessageCreateEvent event) {
        Optional<Long> l; try {l = Optional.of(Long.parseLong(s));} catch (NumberFormatException e) {l = Optional.empty();}
        if (l.isPresent()) return l.get();
        Optional<Double> d; try {d = Optional.of(Double.parseDouble(s));} catch (NumberFormatException e) {d = Optional.empty();}
        if (d.isPresent()) return d.get();
        // TODO: Fix this, manual check
        Optional<Boolean> b = Optional.empty();
        if (s.equalsIgnoreCase("true")) b = Optional.of(true);
        if (s.equalsIgnoreCase("false")) b = Optional.of(false);
        if (b.isPresent()) return b.get();

        if (s.endsWith(">") && s.length() > 3) {
            // TODO: Verify that it is used in a guild
            if (s.startsWith("<@&")) {
                if (event.getGuildId().isPresent()) {
                    return client.getRoleById(event.getGuildId().get(), Snowflake.of(Id.of(s.substring(3, s.length() - 1)))).block();
                }
            }
            else if (s.startsWith("<@")) return client.getUserById(Snowflake.of(Id.of(s.substring(2, s.length() - 1)))).block();
            else if (s.startsWith("<#")) return client.getChannelById(Snowflake.of(Id.of(s.substring(2, s.length() - 1)))).block();
        }

        return s;
    }

    /**
     * Creates the parameters
     * @param event
     * @param commandParams
     * @return
     */
    private List<Object> getOrderedMethodParams(MessageCreateEvent event, List<String> commandParams, Method method) {
        int realParamCount = (int)Arrays.stream(method.getParameters()).filter(x -> x.isAnnotationPresent(CommandParam.class)).count();
        int curRealParam = 0;
        List<Object> paramsRet = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(CommandParam.class)) {
                curRealParam++;
            } else if (param.isAnnotationPresent(EventUser.class)) {
                paramsRet.add(client.getUserById(Snowflake.of(event.getMessage().getUserData().id())).block());
            } else if (param.isAnnotationPresent(EventUserId.class)) {
                paramsRet.add(event.getMessage().getUserData().id().asLong());
            }
        }
        return paramsRet;
    }
}
