package io.github.stelitop.mad4j.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
    double number() default 0;
    String string() default "";
    boolean bool() default false;

}
