package net.stelitop.generalbot.commandrequirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.mad4j.requirements.CommandRequirementExecutor;
import net.stelitop.mad4j.utils.ActionResult;
import org.springframework.stereotype.Component;

@Component
public class UnusableCommandImplementation implements CommandRequirementExecutor {
    @Override
    public ActionResult<Void> verify(ChatInputInteractionEvent event) {
        return ActionResult.fail("This command is unusable!");
    }
}
