package io.github.stelitop.mad4j.events;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SlashCommandEmbedCreateSpecEventResult implements AllowedEventResult {
    @Override
    public List<Class<?>> resultTypes() {
        return List.of(EmbedCreateSpec.class);
    }

    @Override
    public List<Class<? extends Event>> eventTypes() {
        return List.of(ChatInputInteractionEvent.class);
    }

    @Override
    public Mono<Void> transform(Object result, Event event) {
        if (result instanceof EmbedCreateSpec r && event instanceof ChatInputInteractionEvent e) {
            return e.reply().withEmbeds(r);
        }
        throw new IllegalArgumentException();
    }
}
