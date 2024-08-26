package io.github.stelitop.mad4j;

import io.github.stelitop.mad4j.commands.SlashCommand;
import io.github.stelitop.mad4j.commands.components.ComponentInteraction;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A special Spring component that contains Discord interactions. Inside you can annotate
 * methods with {@link SlashCommand}, {@link ComponentInteraction} and other such events. They also
 * act as regular Spring components, allowing other dependencies to be Autowired.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DiscordEventsComponent {

}
