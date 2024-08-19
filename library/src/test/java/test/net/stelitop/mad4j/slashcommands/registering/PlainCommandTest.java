package test.net.stelitop.mad4j.slashcommands.registering;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.service.ApplicationService;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.commands.InteractionEvent;
import net.stelitop.mad4j.commands.CommandParam;
import net.stelitop.mad4j.commands.SlashCommand;
import test.net.stelitop.mad4j.slashcommands.BaseTestConfiguration;
import net.stelitop.mad4j.utils.OptionType;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import({BaseTestConfiguration.class, PlainCommandTest.TestComponent.class})
public class PlainCommandTest {

    @Autowired
    private ApplicationService applicationServiceMock;

    @DiscordEventsComponent
    public static class TestComponent {

        static final String commandDescription = "Adds two numbers.";
        static final String param1Description = "The first number.";
        static final String param2Description = "The second number.";

        @SlashCommand(name = "add", description = commandDescription)
        public Mono<Void> addTwoNumbersCommand(
                @InteractionEvent ChatInputInteractionEvent event,
                @CommandParam(name = "x", description = param1Description) long x,
                @CommandParam(name = "y", description = param2Description) long y
        ) {
            return event.reply("The sum is " + x + y);
        }
    }

    @Test
    public void testLoadingCommand() {
        ArgumentCaptor<List<ApplicationCommandRequest>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(applicationServiceMock, times(1)).bulkOverwriteGlobalApplicationCommand(
                eq(BaseTestConfiguration.TEST_APPLICATION_ID), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).hasSize(1);
        ApplicationCommandRequest request = argumentCaptor.getValue().get(0);
        assertThat(request.name()).isEqualTo("add");
        assertThat(request.description().get()).isEqualTo(TestComponent.commandDescription);
        assertThat(request.options().get()).containsExactly(
                ApplicationCommandOptionData.builder().name("x").type(OptionType.INTEGER).required(true)
                        .description(TestComponent.param1Description).build(),
                ApplicationCommandOptionData.builder().name("y").type(OptionType.INTEGER).required(true)
                        .description(TestComponent.param2Description).build()
        );
    }
}
