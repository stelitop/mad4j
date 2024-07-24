package net.stelitop.generalbot.commandrequirements;

import net.stelitop.mad4j.requirements.CommandRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CommandRequirement(implementation = UnusableCommandImplementation.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnusableCommand {
}
