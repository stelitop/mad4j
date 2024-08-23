package net.stelitop.mad4j.events;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SlashCommandStringEventResult implements AllowedEventResult {

    @Override
    public Class<?> resultType() {
        return String.class;
    }

    @Override
    public Class<? extends Event> eventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> transform(Object result, Event event) {
        if (result instanceof String s && event instanceof ChatInputInteractionEvent e) {
            return e.reply(s);
        }
        throw new IllegalArgumentException();
    }
}
