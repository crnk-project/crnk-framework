package io.crnk.core.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Defines the version range the given type or field is applicable. Only types and fields with ranges that
 * fall into the request version are returned. A version is represented as an integer number.
 * <p>
 * Currently considered experimental.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface JsonApiVersion {

    int min() default 0;

    int max() default Integer.MAX_VALUE;

}