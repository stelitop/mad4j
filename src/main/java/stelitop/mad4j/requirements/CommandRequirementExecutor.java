package stelitop.mad4j.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import stelitop.mad4j.utils.ActionResult;

public interface CommandRequirementExecutor {

    ActionResult<Void> verify(ChatInputInteractionEvent event);
}
