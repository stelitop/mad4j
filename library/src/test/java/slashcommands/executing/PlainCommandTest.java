package slashcommands.executing;


import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.service.ApplicationService;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.CommandParam;
import net.stelitop.mad4j.commands.InteractionEvent;
import net.stelitop.mad4j.commands.SlashCommand;
import net.stelitop.mad4j.listeners.SlashCommandListener;
import net.stelitop.mad4j.utils.OptionType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import slashcommands.BaseTestConfiguration;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import({BaseTestConfiguration.class, PlainCommandTest.TestComponent.class})
public class PlainCommandTest {

    @Autowired
    private GatewayDiscordClient gatewayDiscordClientMock;
    @Autowired
    private ApplicationService applicationServiceMock;
    @Autowired
    private SlashCommandListener slashCommandListener;
    @DiscordEventsComponent
    public static class TestComponent {

        static final String commandDescriptionJoke = "Tells a joke";
        static final String joke = "Why did the chicken cross the road?";

        @SlashCommand(name = "joke", description = commandDescriptionJoke)
        public Mono<Void> tellJokeCommand(
                @InteractionEvent ChatInputInteractionEvent event
        ) {
            return event.reply(joke);
        }

        static final String commandDescriptionAdd = "Adds two numbers together";

        @SlashCommand(name = "add", description = commandDescriptionAdd)
        public Mono<Void> addTwoNumbersCommand(
                @InteractionEvent ChatInputInteractionEvent event,
                @CommandParam(name = "x", description = "not relevant") long x,
                @CommandParam(name = "y", description = "not relevant") long y
        ) {
            return event.reply(String.valueOf(x + y));
        }

        static final String commandDescriptionOptional = "Command with an optional parameter.";

        @SlashCommand(name = "optional", description = commandDescriptionOptional)
        public Mono<Void> optionalParamCommand(
                @InteractionEvent ChatInputInteractionEvent event,
                @CommandParam(name = "string", description = "not relevant", required = false) String s
        ) {
            if (s == null) {
                return event.reply("No value");
            } else {
                return event.reply("String = " + s);
            }
        }
    }

    @Test
    public void noParamsCommandJoke() {
        var event = new ChatInputInteractionEvent(gatewayDiscordClientMock, null, new Interaction(gatewayDiscordClientMock,
                ImmutableInteractionData.of(
                        Id.of("0"),
                        Id.of("0"),
                        1, // Chat application command type
                        Possible.of(ApplicationCommandInteractionData.builder()
                                .name("joke")
                                .build()),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        "token",
                        1, // version
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent()
                )));
        var eventSpy = spy(event);
        slashCommandListener.handle(eventSpy);

        verify(eventSpy).reply(TestComponent.joke);
        verify(eventSpy, times(1)).reply((String) any());
    }

    @Test
    public void paramsCommandAdd() {
        long x = 5, y = 14;
        var event = new ChatInputInteractionEvent(gatewayDiscordClientMock, null, new Interaction(gatewayDiscordClientMock,
                ImmutableInteractionData.of(
                        Id.of("0"),
                        Id.of("0"),
                        1, // Chat application command type
                        Possible.of(ApplicationCommandInteractionData.builder()
                                .name("add")
                                .addOption(ApplicationCommandInteractionOptionData.builder().name("x").value(String.valueOf(x)).type(OptionType.INTEGER).build())
                                .addOption(ApplicationCommandInteractionOptionData.builder().name("y").value(String.valueOf(y)).type(OptionType.INTEGER).build())
                                .build()),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        "token",
                        1, // version
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent()
                )));
        var eventSpy = spy(event);
        slashCommandListener.handle(eventSpy);

        verify(eventSpy).reply(String.valueOf(x + y));
        verify(eventSpy, times(1)).reply((String) any());
    }

    @Test
    public void optionalParamPresent() {
        String inputString = "abcdef12345";
        var event = new ChatInputInteractionEvent(gatewayDiscordClientMock, null, new Interaction(gatewayDiscordClientMock,
                ImmutableInteractionData.of(
                        Id.of("0"),
                        Id.of("0"),
                        1, // Chat application command type
                        Possible.of(ApplicationCommandInteractionData.builder()
                                .name("optional")
                                .addOption(ApplicationCommandInteractionOptionData.builder().name("string").value(inputString).type(OptionType.STRING).build())
                                .build()),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        "token",
                        1, // version
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent()
                )));
        var eventSpy = spy(event);
        slashCommandListener.handle(eventSpy);

        verify(eventSpy).reply(contains(inputString));
        verify(eventSpy, times(1)).reply((String) any());
    }

    @Test
    public void optionalParamMissing() {
        String inputString = "abcdef12345";
        var event = new ChatInputInteractionEvent(gatewayDiscordClientMock, null, new Interaction(gatewayDiscordClientMock,
                ImmutableInteractionData.of(
                        Id.of("0"),
                        Id.of("0"),
                        1, // Chat application command type
                        Possible.of(ApplicationCommandInteractionData.builder()
                                .name("optional")
                                .build()),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        "token",
                        1, // version
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent(),
                        Possible.absent()
                )));
        var eventSpy = spy(event);
        slashCommandListener.handle(eventSpy);

        verify(eventSpy).reply("No value");
        verify(eventSpy, times(1)).reply((String) any());
    }
}
