package io.github.stelitop.mad4j.commands.components;

import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import org.intellij.lang.annotations.RegExp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentInteraction {
    Class<? extends ComponentInteractionEvent> event();
    @RegExp
    String regex();
}
