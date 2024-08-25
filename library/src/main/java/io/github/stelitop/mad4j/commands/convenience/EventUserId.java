package io.github.stelitop.mad4j.commands.convenience;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be annotated to a {@link Long} or long parameter in the method
 * signature of either a slash command or a component interaction to
 * automatically inject the user id of the user who created the event
 * into the value.
 */
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventUserId {
}
