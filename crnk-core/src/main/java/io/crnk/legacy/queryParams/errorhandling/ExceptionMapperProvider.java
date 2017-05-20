package io.crnk.legacy.queryParams.errorhandling;

import java.lang.annotation.*;

/**
 * Marks an implementation of an exception mapper that should be discovered by Crnk during startup
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface ExceptionMapperProvider {
}
