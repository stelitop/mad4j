package net.stelitop.generalbot.commands.text;

import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.Command;
import net.stelitop.mad4j.commands.CommandType;
import net.stelitop.mad4j.interactions.EventResponse;

@DiscordEventsComponent
public class BasicTextCommand {

    @Command(
            name = "textping",
            description = "Replies back with pong.",
            types = {CommandType.Text}
    )
    public EventResponse pingCommand() {
        return EventResponse.createPlaintext("pong");
    }
}
