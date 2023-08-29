package net.stelitop.mad4j;


import net.stelitop.mad4j.commands.SlashCommand;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A command component is a class that contains one or multiple
 * {@link SlashCommand} command methods. These classes are also
 * part of the Spring ecosystem.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DiscordEventsComponent {

}
