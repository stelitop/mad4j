package net.stelitop.mad4j.convenience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be annotated to a {@link discord4j.core.object.entity.User} object in
 * the method signature of either a slash command or a component interaction
 * to automatically inject the user who created the event into the value.
 */
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventUser {
}
