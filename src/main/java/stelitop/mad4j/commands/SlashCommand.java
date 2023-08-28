package stelitop.mad4j.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommand {
    /**
     * The name of the slash command.
     *
     * @return The name of the slash command.
     */
    String name();
    /**
     * The description of the slash command.
     *
     * @return The description of the slash command.
     */
    String description();
}
