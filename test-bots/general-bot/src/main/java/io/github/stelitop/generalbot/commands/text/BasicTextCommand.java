package io.github.stelitop.generalbot.commands.text;

import io.github.stelitop.mad4j.DiscordEventsComponent;
import io.github.stelitop.mad4j.commands.Command;
import io.github.stelitop.mad4j.commands.CommandType;
import io.github.stelitop.mad4j.interactions.EventResponse;

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
