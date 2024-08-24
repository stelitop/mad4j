package net.stelitop.mad4j.events;

import discord4j.core.event.domain.Event;
import net.stelitop.mad4j.utils.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AllowedEventResultHandler {

    private final List<AllowedEventResult> responses;

    @Autowired
    public AllowedEventResultHandler(List<AllowedEventResult> responses) {
        this.responses = responses;
        verifyNoOverlap();
    }

    public ActionResult<Mono<Void>> handleEventResult(Object result, Event event) {
        var allowedEventResult = getMatchingResult(result, event);
        if (allowedEventResult == null) return ActionResult.fail("This event result type is not allowed!");
        return ActionResult.success(allowedEventResult.transform(result, event));
    }

    private AllowedEventResult getMatchingResult(Object result, Event event) {
        for (var response : responses) {
            if (response.resultTypes().stream().anyMatch(r -> r.isAssignableFrom(result.getClass()))
                    && response.eventTypes().stream().anyMatch(e -> e.isAssignableFrom(event.getClass()))) {
                return response;
            }
        }
        return null;
    }

    private void verifyNoOverlap() {
        for (int i = 0; i < responses.size(); i++) {
            for (int j = i + 1; j < responses.size(); j++) {
                var x = responses.get(i);
                var y = responses.get(j);
                // actually implement allowing multiple things
            }
        }
    }
}
