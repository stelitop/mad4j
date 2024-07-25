package net.stelitop.mad4j.commands.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;

import java.util.List;

public class NullAutocompleteExecutor implements AutocompletionExecutor {

    private NullAutocompleteExecutor() {

    }

    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        return List.of();
    }
}
