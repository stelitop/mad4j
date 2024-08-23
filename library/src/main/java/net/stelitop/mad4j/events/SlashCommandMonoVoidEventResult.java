package net.stelitop.mad4j.events;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SlashCommandMonoVoidEventResult implements AllowedEventResult {
    @Override
    public Class<?> resultType() {
        return Mono.class;
    }

    @Override
    public Class<? extends Event> eventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> transform(Object result, Event event) {
        if (!Mono.class.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException();
        }
        return ((Mono<?>) result).cast(Void.class);
    }
}
