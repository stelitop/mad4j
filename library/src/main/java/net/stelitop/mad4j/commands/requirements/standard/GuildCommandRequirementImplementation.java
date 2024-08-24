package net.stelitop.mad4j.commands.requirements.standard;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.requirements.CommandRequirement;
import net.stelitop.mad4j.commands.requirements.CommandRequirementExecutor;
import net.stelitop.mad4j.utils.ActionResult;
import org.springframework.stereotype.Component;

@DiscordEventsComponent
public class GuildCommandRequirementImplementation implements CommandRequirementExecutor {
    @Override
    public ActionResult<Void> verify(ChatInputInteractionEvent event) {
        boolean inGuild = event.getInteraction().getGuildId().isPresent();
        if (inGuild) {
            return ActionResult.success();
        } else {
            return ActionResult.fail("This command only works in a server!");
        }
    }
}
