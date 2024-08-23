package net.stelitop.mad4j.events;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

/**
 * Takes care of different types of allowed responses for different types of events.
 */
public interface AllowedEventResult {

//    /**
//     * Verifies that the response value is of the wanted type for this handler.
//     * @param object Returned value
//     *
//     * @return True if the
//     */
//    default boolean verify(Object object) {
//
//    }
    Class<?> resultType();
    Class<? extends Event> eventType();

    Mono<Void> transform(Object result, Event event);
}
