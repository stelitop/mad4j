package io.github.stelitop.mad4j.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation for all commands to be used by the bot.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The name of the command.
     */
    String name();
    /**
     * The description of the command.
     */
    String description();
    /**
     * The types this command registers as. Can be multiple types. The existing types
     * are "text" for commands from messages used a prefix, and "slash" for slash commands.
     * If no types are given, then the default type is slash commands only, unless changed
     * in the properties file.
     */
    // TODO: Replace these with enum types
    CommandType[] types() default {};
}
