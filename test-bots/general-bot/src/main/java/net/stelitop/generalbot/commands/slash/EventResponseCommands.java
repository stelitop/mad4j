package net.stelitop.generalbot.commands.slash;


import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.InteractionEvent;
import net.stelitop.mad4j.commands.SlashCommand;
import net.stelitop.mad4j.commands.components.ComponentInteraction;
import net.stelitop.mad4j.interactions.EventResponse;
import reactor.core.publisher.Mono;

import java.util.Random;

@DiscordEventsComponent
public class EventResponseCommands {

    @SlashCommand(
            name = "eventresponse normal plaintext",
            description = "Uses EventResponse instead of directly replying to the event. Returns plaintext."
    )
    public EventResponse normalPlaintext() {
        return EventResponse.createPlaintext("This was created using an event response!");
    }

    @SlashCommand(
            name = "eventresponse normal embed",
            description = "Uses EventResponse instead of directly replying to the event. Returns an embed."
    )
    public EventResponse normalEmbed() {
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.TAHITI_GOLD)
                .title("Fancy embed!")
                .description("Embed description.")
                .build();
        return EventResponse.createEmbed(embed);
    }

    @ComponentInteraction(
            regex = "ButtonEventResponseEditPlainText",
            event = ButtonInteractionEvent.class
    )
    public EventResponse eventResponseButtonEditPlaintext() {
        return EventResponse.editPlaintext("Edited text! Random number: " + new Random().nextInt(10000));
    }

    @SlashCommand(
            name = "eventresponse button editplaintext",
            description = "Uses EventResponse instead of directly replying to the event. Returns an embed."
    )
    public EventResponse buttonWithResponseEditPlaintext() {
        return EventResponse.createPlaintext("Click the button for it to reply with an event response.")
                .components(ActionRow.of(Button.primary("ButtonEventResponseEditPlainText", "Event response button - edit plaintext.")));
    }

    @ComponentInteraction(
            regex = "ButtonEventResponseCreatePlainText",
            event = ButtonInteractionEvent.class
    )
    public EventResponse eventResponseButtonCreatePlaintext() {
        return EventResponse.createPlaintext("Created new text!");
    }

    @SlashCommand(
            name = "eventresponse button createplaintext",
            description = "Uses EventResponse instead of directly replying to the event. Returns an embed."
    )
    public Mono<Void> buttonWithResponseCreatePlaintext(@InteractionEvent ChatInputInteractionEvent event) {

        return event.reply("Click the button for it to reply with an event response")
                .withComponents(ActionRow.of(Button.primary("ButtonEventResponseCreatePlainText", "Event response button - create plaintext.")));
    }
}
