package io.github.stelitop.generalbot.commands.slash;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import io.github.stelitop.generalbot.commandrequirements.UnusableCommand;
import io.github.stelitop.mad4j.DiscordEventsComponent;
import io.github.stelitop.mad4j.commands.InteractionEvent;
import io.github.stelitop.mad4j.commands.CommandParam;
import io.github.stelitop.mad4j.commands.DefaultValue;
import io.github.stelitop.mad4j.commands.SlashCommand;
import io.github.stelitop.mad4j.commands.requirements.standard.DMCommandRequirement;
import io.github.stelitop.mad4j.commands.requirements.standard.GuildCommandRequirement;
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

    @SlashCommand(
            name = "basic dice",
            description = "Rolls a 6-sided die. Tests simplest command with no parameters."
    )
    public Mono<Void> diceRollEmpty(@InteractionEvent ChatInputInteractionEvent event) {
        int number = random.nextInt(6) + 1;
        return event.reply("You rolled " + number + "!");
    }

    @SlashCommand(
            name = "basic add",
            description = "Adds two numbers together. Tests simple command parameters."
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
            name = "basic number",
            description = "Inputs an optional number. Tests optional parameters with no default value."
    )
    public Mono<Void> number(
            @InteractionEvent
            ChatInputInteractionEvent event,
            @CommandParam(name = "value", description = "Number to input.", required = false)
            Long value
    ) {
        if (value == null) return event.reply("No number was given!");
        else return event.reply("The number is " + value + "!");
    }

    @SlashCommand(
            name = "basic greet",
            description = "Greets a person. Bob by default. Tests default values to optional parameters."
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
            description = "This command should return an error message. Tests command requirements."
    )
    public Mono<Void> unusableCommand(
            @InteractionEvent
            ChatInputInteractionEvent event
    ) {
        return event.reply("Used! This is an error, the requirement is not working.");
    }

    @SlashCommand(
            name = "basic userchannel",
            description = "Tests the injection of user and channel parameters."
    )
    public Mono<Void> userchanneLCommand(
            @InteractionEvent
            ChatInputInteractionEvent event,
            @CommandParam(name = "user", description = "User")
            User user
    ) {
        return event.reply("User = " + user.getTag());
    }

    @SlashCommand(
            name = "basic stringresponse",
            description = "Responses by returning a string."
    )
    public String stringResponse() {
        return "This was returned as a string!";
    }

    @SlashCommand(
            name = "basic embedresponse",
            description = "Responses by returning a string."
    )
    public EmbedCreateSpec embedResponse() {
        return EmbedCreateSpec.builder()
                .title("Embed Title")
                .description("Embed Description")
                .color(Color.BLUE)
                .build();
    }

    @GuildCommandRequirement
    @SlashCommand(
            name = "basic guildonly",
            description = "Only usable in guilds."
    )
    public String guildOnlyCommand() {
        return "Hello guild!";
    }

    @DMCommandRequirement
    @SlashCommand(
            name = "basic dmonly",
            description = "Only usable in guilds."
    )
    public String dmOnlyCommand() {
        return "Hello dms!";
    }
}
