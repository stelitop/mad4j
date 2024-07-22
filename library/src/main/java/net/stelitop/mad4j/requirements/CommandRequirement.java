package net.stelitop.mad4j.requirements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * <p>Annotation used for creating command requirements that limit when a command can be used.
 * To use this, you need a requirement annotation to use on command methods and an implementation
 * of the requirement.</p>
 *
 * <p>The implementation of the requirement must be a class that has a Spring bean
 * and implement the {@link CommandRequirementExecutor} interface. A regular {@link Component} works.</p>
 *
 * <p>The annotation that would represent the requirement needs the {@link CommandRequirement} annotation
 * and a link to the {@link CommandRequirementExecutor} related to it. Furthermore,
 * the annotation requires <code>@Retention(RetentionPolicy.RUNTIME)</code> and
 * <code>@Target(ElementType.METHOD)</code> for it to be correctly recognised.</p>
 *
 * @see CommandRequirementExecutor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.ANNOTATION_TYPE})
public @interface CommandRequirement {

    /**
     * The class that implements this condition and that is called when
     * the check is executed. The class must have a declared Spring bean to be detected.
     *
     * @return The class.
     */
    Class<? extends CommandRequirementExecutor> implementation();
}
