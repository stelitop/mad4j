package net.stelitop.generalbot.commands.slashcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.generalbot.commandrequirements.UnusableCommand;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.InteractionEvent;
import net.stelitop.mad4j.commands.CommandParam;
import net.stelitop.mad4j.commands.DefaultValue;
import net.stelitop.mad4j.commands.SlashCommand;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Random;

@DiscordEventsComponent
public class BasicCommands {

    private Random random;

    @Autowired
    public BasicCommands() {
        this.random = new Random();
    }

    /**
     * Tests the simplest form of a slash command - no extra parameters.
     */
    @SlashCommand(
            name = "basic dice",
            description = "Rolls a 6-sided die!"
    )
    public Mono<Void> diceRollEmpty(@InteractionEvent ChatInputInteractionEvent event) {
        int number = random.nextInt(6) + 1;
        return event.reply("You rolled " + number + "!");
    }

    /**
     * Tests simple command parameters.
     */
    @SlashCommand(
            name = "basic add",
            description = "Adds two numbers together."
    )
    public Mono<Void> addNumbers(
            @InteractionEvent
            ChatInputInteractionEvent event,
            @CommandParam(name = "a", description = "First number")
            long a,
            @CommandParam(name = "b", description = "Second number")
            long b
    ) {
        return event.reply(String.valueOf(a + b));
    }

    @SlashCommand(
            name = "basic greet",
            description = "Greets a person. Bob by default."
    )
    public Mono<Void> greeting(
            @InteractionEvent
            ChatInputInteractionEvent event,
            @DefaultValue(string = "Bob")
            @CommandParam(name = "name", description = "Person's name. Defaults to Bob.", required = false)
            String name
    ) {
        return event.reply("Nice to meet you, " + name + "!");
    }

    @UnusableCommand
    @SlashCommand(
            name = "basic unusable",
            description = "This command should return an error message."
    )
    public Mono<Void> evenTime(
            @InteractionEvent
            ChatInputInteractionEvent event
    ) {
        return event.reply("Used! This is an error, the requirement is not working.");
    }
}
