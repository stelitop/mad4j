package net.stelitop.mad4j.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageListener implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final ApplicationContext applicationContext;
    private final GatewayDiscordClient client;

    @Autowired
    public MessageListener(ApplicationContext applicationContext, GatewayDiscordClient client) {
        this.applicationContext = applicationContext;
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) {
        client.on(MessageCreateEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(MessageCreateEvent event) {
        String content = event.getMessage().getContent();
        System.out.println("Message content = " + content);
        return Mono.empty();
    }
}
