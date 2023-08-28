package stelitop.mad4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The parameter that contains the {@link discord4j.core.event.domain.interaction.ChatInputInteractionEvent}
 * event object for the slash command.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface InteractionEvent {
}
