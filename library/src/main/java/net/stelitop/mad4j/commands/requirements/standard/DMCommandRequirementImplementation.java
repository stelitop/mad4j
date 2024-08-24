package net.stelitop.mad4j.commands.requirements.standard;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.requirements.CommandRequirement;
import net.stelitop.mad4j.commands.requirements.CommandRequirementExecutor;
import net.stelitop.mad4j.utils.ActionResult;
import org.springframework.stereotype.Component;

@DiscordEventsComponent
public class DMCommandRequirementImplementation implements CommandRequirementExecutor {
    @Override
    public ActionResult<Void> verify(ChatInputInteractionEvent event) {
        MessageChannel channel = event.getInteraction().getChannel().block();
        if (channel == null) {
            throw new NullPointerException("Could not get the channel of an interaction!");
        }
        boolean inPrivate = channel.getType().equals(Channel.Type.DM);
        if (inPrivate) {
            return ActionResult.success();
        } else {
            return ActionResult.fail("This command only works in DMs!");
        }
    }
}
