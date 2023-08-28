package stelitop.mad4j.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//  @Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParamChoice {

    /**
     * The display name of the option.
     * @return The name of the option.
     */
    String name();

    /**
     * The value that will be injected into the option if selected.
     * @return The value of the option.
     */
    String value();
}
