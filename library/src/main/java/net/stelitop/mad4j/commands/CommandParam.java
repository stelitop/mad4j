package net.stelitop.mad4j.commands;

import net.stelitop.mad4j.commands.autocomplete.AutocompletionExecutor;
import net.stelitop.mad4j.commands.autocomplete.NullAutocompleteExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A single parameter of a slash command.
 */
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParam {
    /**
     * The name of the parameter that will be shown to the user.
     */
    String name();

    /**
     * The description of the parameter that will be shown to the user.
     */
    String description();

    /**
     * The specific choices that the user can choose from for this parameter.
     * Optional setting.
     */
    CommandParamChoice[] choices() default {};
    /**
     * Whether the user must fill out this option. If set to false, the user
     * can decide to leave this option empty. In this case, null will be injected
     * into the field, unless there is a default value given.
     */
    boolean required() default true;

    /**
     * The class that gives autofill suggestions for this method.
     */
    Class<? extends AutocompletionExecutor> autocomplete() default NullAutocompleteExecutor.class;

    /**
     * The minimum value that this parameter must take. Only works for numerical params.
     */
    double minValue() default Double.MIN_VALUE;
    /**
     * The maximum value that this parameter must take. Only works for numerical params.
     */
    double maxValue() default Double.MAX_VALUE;

    /**
     * The minimum length for a string parameter.
     */
    int minLength() default 0;

    /**
     * The maximum length for a string parameter.
     */
    int maxLength() default Integer.MAX_VALUE;
}