package test.io.github.stelitop.mad4j.slashcommands.registering;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import io.github.stelitop.mad4j.DiscordEventsComponent;
import io.github.stelitop.mad4j.commands.CommandParam;
import io.github.stelitop.mad4j.commands.DefaultValue;
import io.github.stelitop.mad4j.commands.InteractionEvent;
import io.github.stelitop.mad4j.commands.SlashCommand;
import io.github.stelitop.mad4j.commands.convenience.EventUser;
import io.github.stelitop.mad4j.commands.convenience.EventUserId;
import test.io.github.stelitop.mad4j.slashcommands.BaseTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IncorrectCommandMethodDefinitionTest {

    public static class LongNameTest {
        private static final String commandName = "name with too many parts";
        @DiscordEventsComponent
        public static class TestComponent {

            @SlashCommand(name = commandName, description = "Not relevant")
            public Mono<Void> longNameCommand(
                    @InteractionEvent ChatInputInteractionEvent event
            ) {
                return event.reply("Reply!");
            }
        }

        @EnableAutoConfiguration
        @Import({BaseTestConfiguration.class, TestComponent.class})
        protected static class TestApplication {

        }

        @Test
        public void test() {
//            Logger registrarLogger = (Logger) LoggerFactory.getLogger(SlashCommandRegistrar.class);
//            ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
//            listAppender.start();
//            registrarLogger.addAppender(listAppender);

            SpringApplication springApplication = new SpringApplication(TestApplication.class);
            assertThrows(RuntimeException.class, springApplication::run);

//            System.out.println(listAppender.list.size());

//            assertTrue(listAppender.list.stream()
//                    .anyMatch(x -> x.getLevel().equals(Level.ERROR) && x.getFormattedMessage().contains(commandName)));
        }
    }

    public static class InvalidParamTypeTest {
        @DiscordEventsComponent
        public static class TestComponent {

            public static class CustomType {

            }
            @SlashCommand(name = "notrelevant", description = "Not relevant")
            public Mono<Void> longNameCommand(
                    @InteractionEvent ChatInputInteractionEvent event,
                    @CommandParam(name = "param", description = "Not relevant") CustomType par
            ) {
                return event.reply("Reply!");
            }
        }

        @EnableAutoConfiguration
        @Import({BaseTestConfiguration.class, TestComponent.class})
        protected static class TestApplication {

        }

        @Test
        public void test() {
            SpringApplication springApplication = new SpringApplication(TestApplication.class);
            assertThrows(RuntimeException.class, springApplication::run);
        }
    }

    public static class PrimitiveParamWithDefaultValueTest {
        @DiscordEventsComponent
        public static class TestComponent {

            public static class CustomType {

            }
            @SlashCommand(name = "notrelevant", description = "Not relevant")
            public Mono<Void> longNameCommand(
                    @InteractionEvent ChatInputInteractionEvent event,
                    @CommandParam(name = "param", description = "Not relevant", required = false)
                    @DefaultValue(number = 123)
                    long par
            ) {
                return event.reply("Reply!");
            }
        }

        @EnableAutoConfiguration
        @Import({BaseTestConfiguration.class, TestComponent.class})
        protected static class TestApplication {

        }

        @Test
        public void test() {
            SpringApplication springApplication = new SpringApplication(TestApplication.class);
            assertThrows(RuntimeException.class, springApplication::run);
        }
    }

    public static class EventUserInjectionWrongTypeTest {
        @DiscordEventsComponent
        public static class TestComponent {

            public static class CustomType {

            }
            @SlashCommand(name = "notrelevant", description = "Not relevant")
            public Mono<Void> longNameCommand(
                    @InteractionEvent ChatInputInteractionEvent event,
                    @EventUser long user
            ) {
                return event.reply("Reply!");
            }
        }

        @EnableAutoConfiguration
        @Import({BaseTestConfiguration.class, TestComponent.class})
        protected static class TestApplication {

        }

        @Test
        public void test() {
            SpringApplication springApplication = new SpringApplication(TestApplication.class);
            assertThrows(RuntimeException.class, springApplication::run);
        }
    }

    public static class EventUserIdInjectionWrongTypeTest {
        @DiscordEventsComponent
        public static class TestComponent {

            public static class CustomType {

            }
            @SlashCommand(name = "notrelevant", description = "Not relevant")
            public Mono<Void> longNameCommand(
                    @InteractionEvent ChatInputInteractionEvent event,
                    @EventUserId double userId
            ) {
                return event.reply("Reply!");
            }
        }

        @EnableAutoConfiguration
        @Import({BaseTestConfiguration.class, TestComponent.class})
        protected static class TestApplication {

        }

        @Test
        public void test() {
            SpringApplication springApplication = new SpringApplication(TestApplication.class);
            assertThrows(RuntimeException.class, springApplication::run);
        }
    }
}
