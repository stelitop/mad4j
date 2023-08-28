package stelitop.mad4j.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import stelitop.mad4j.utils.OptionType;
import stelitop.mad4j.autocomplete.AutocompletionExecutor;
import stelitop.mad4j.autocomplete.InputSuggestion;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommandOptionAutocompleteListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final GatewayDiscordClient client;
    private final Map<Class<? extends AutocompletionExecutor>, AutocompletionExecutor> autocompletionExecutorBeans;
    private final Map<Pair<String, String>, Class<? extends AutocompletionExecutor>> commandNameParamToExecutor;

    @Autowired
    public CommandOptionAutocompleteListener(
            GatewayDiscordClient client,
            List<AutocompletionExecutor> autocompletionExecutors
    ) {
        this.client = client;
        this.autocompletionExecutorBeans = autocompletionExecutors.stream()
                .collect(Collectors.toMap(AutocompletionExecutor::getClass, x -> x));
        this.commandNameParamToExecutor = new HashMap<>();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ChatInputAutoCompleteEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ChatInputAutoCompleteEvent event) {
        String commandName = getCommandName(event);
        String paramName = event.getFocusedOption().getName();
        Class<? extends AutocompletionExecutor> executorClass = commandNameParamToExecutor.get(Pair.of(commandName, paramName));
        if (executorClass == null) {
            LOGGER.error("Command \"" + commandName + "\" had no binded autocompleter for param \"" + paramName + "\"!");
            return Mono.empty();
        }
        AutocompletionExecutor executor = autocompletionExecutorBeans.get(executorClass);
        if (executor == null) {
            LOGGER.error("Command \"" + commandName + "\" had no autocompleter implementation for param \"" + paramName + "\"!");
            return Mono.empty();
        }

        List<InputSuggestion> suggestions = executor.execute(event);
        List<ApplicationCommandOptionChoiceData> ret = new ArrayList<>();
        suggestions.stream()
                .limit(25)
                .map(x -> ApplicationCommandOptionChoiceData.builder().name(x.getName()).value(x.getValue()).build())
                .forEach(ret::add);
        return event.respondWithSuggestions(ret);
    }

    private String getCommandName(ChatInputAutoCompleteEvent event) {
        String name = event.getCommandName();
        List<ApplicationCommandInteractionOption> options = event.getOptions();
        while (options.size() == 1 && (options.get(0).getType().getValue() == OptionType.SUB_COMMAND || options.get(0).getType().getValue() == OptionType.SUB_COMMAND_GROUP)) {
            name += " " + options.get(0).getName();
            options = options.get(0).getOptions();
        }
        return name.toLowerCase();
    }

    public void addMapping(String commandName, String paramName, Class<? extends AutocompletionExecutor> implementation) {
        commandNameParamToExecutor.put(Pair.of(commandName, paramName), implementation);
    }
}
