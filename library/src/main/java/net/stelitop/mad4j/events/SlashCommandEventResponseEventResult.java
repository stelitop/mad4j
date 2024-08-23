package net.stelitop.mad4j.events;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.mad4j.interactions.EventResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SlashCommandEventResponseEventResult implements AllowedEventResult {
    @Override
    public Class<?> resultType() {
        return EventResponse.class;
    }

    @Override
    public Class<? extends Event> eventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> transform(Object result, Event event) {
        if (!EventResponse.class.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException();
        }
        if (!ChatInputInteractionEvent.class.isAssignableFrom(event.getClass())) {
            throw new IllegalArgumentException();
        }
        var r = (EventResponse) result;
        ChatInputInteractionEvent e = (ChatInputInteractionEvent) event;
        return r.respond(e);
    }
}
