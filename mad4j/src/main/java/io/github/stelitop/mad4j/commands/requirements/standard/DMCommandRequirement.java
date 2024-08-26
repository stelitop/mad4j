package io.github.stelitop.mad4j.commands.requirements.standard;

import io.github.stelitop.mad4j.commands.requirements.CommandRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CommandRequirement(implementation = DMCommandRequirementImplementation.class)
public @interface DMCommandRequirement {
}
