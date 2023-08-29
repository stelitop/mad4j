package net.stelitop.mad4j.requirements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate other annotations with this to declare that they imply a condition
 * on a slash command, limiting the cases in which it can be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.ANNOTATION_TYPE})
public @interface CommandRequirement {

    /**
     * The class that implements this condition and that is called when
     * the check is executed.
     *
     * @return The class.
     */
    Class<? extends CommandRequirementExecutor> implementation();
}
