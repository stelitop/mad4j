package io.github.stelitop.generalbot.commandrequirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import io.github.stelitop.mad4j.commands.requirements.CommandRequirementExecutor;
import io.github.stelitop.mad4j.utils.ActionResult;
import org.springframework.stereotype.Component;

@Component
public class UnusableCommandImplementation implements CommandRequirementExecutor {
    @Override
    public ActionResult<Void> verify(ChatInputInteractionEvent event) {
        return ActionResult.fail("This command is unusable!");
    }
}
