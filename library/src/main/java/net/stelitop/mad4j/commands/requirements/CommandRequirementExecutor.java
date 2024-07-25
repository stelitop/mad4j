package net.stelitop.mad4j.commands.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.mad4j.utils.ActionResult;

/**
 * Interface for creating implementations of a custom command requirement. The extending
 * class must be a Spring bean and also connected to a {@link CommandRequirement} annotation.
 *
 * @see CommandRequirement
 */
public interface CommandRequirementExecutor {

    /**
     * Verifies if the conditions for the requirement are met for the given command.
     *
     * @param event The slash command event that is to be verified.
     * @return The response to the event - either a success or a failure with a message.
     *     If failure, the message is returned as the response to the command.
     */
    ActionResult<Void> verify(ChatInputInteractionEvent event);
}
