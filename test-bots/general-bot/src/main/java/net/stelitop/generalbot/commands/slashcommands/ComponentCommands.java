package net.stelitop.generalbot.commands.slashcommands;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.InteractionEvent;
import net.stelitop.mad4j.commands.SlashCommand;
import net.stelitop.mad4j.commands.components.ComponentInteraction;
import reactor.core.publisher.Mono;

import java.util.List;

@DiscordEventsComponent
public class ComponentCommands {

    @ComponentInteraction(
            event = ButtonInteractionEvent.class,
            regex = "testbutton"
    )
    public Mono<Void> testbutton(
            @InteractionEvent
            ButtonInteractionEvent event
    ) {
        return event.reply("Pressed!");
    }

    @SlashCommand(
            name = "component button",
            description = "Creates a button that when pressed sends a message. Tests button interactions."
    )
    public Mono<Void> button(
            @InteractionEvent
            ChatInputInteractionEvent event
    ) {
        return event.reply("Message")
                .withComponents(ActionRow.of(Button.primary("testbutton", "Press me!")));
    }

    @ComponentInteraction(
            event = SelectMenuInteractionEvent.class,
            regex = "testselectmenu"
    )
    public Mono<Void> testselectmenu(
            @InteractionEvent
            SelectMenuInteractionEvent event
    ) {
        return event.reply("Option " + event.getValues().get(0) + " picked!");
    }

    @SlashCommand(
            name = "component selectmenu",
            description = "Creates a button that when pressed sends a message. Tests button interactions."
    )
    public Mono<Void> selectmenu(
            @InteractionEvent
            ChatInputInteractionEvent event
    ) {
        return event.reply("Message")
                .withComponents(ActionRow.of(SelectMenu.of("testselectmenu", List.of(
                        SelectMenu.Option.of("one", "one"),
                        SelectMenu.Option.of("two", "two"),
                        SelectMenu.Option.of("three", "three")
                ))));
    }
}
