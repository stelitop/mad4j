package io.github.stelitop.mad4j.commands.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import io.github.stelitop.mad4j.commands.CommandParam;

import java.util.List;

/**
 * <p>An autocomplete executor returns the suggestion of a specific autocomplete event.
 * This can be connected to a parameter of a slash command by specifying an implementation
 * at {@link CommandParam#autocomplete()}.</p>
 *
 * <p>Classes implementing this interface must also be recognised as beans by spring.</p>
 */
public interface AutocompletionExecutor {

    /**
     * Executes the event of giving suggestions. The event must NOT be
     * answered in this method, as it is answered later on.
     *
     * @param event The event.
     * @return The suggestions for the event.
     */
    List<InputSuggestion> execute(ChatInputAutoCompleteEvent event);
}
