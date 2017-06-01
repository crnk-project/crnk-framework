package io.crnk.legacy.queryParams.errorhandling;

import java.lang.annotation.*;

/**
 * Marks an implementation of an exception mapper that should be discovered by Crnk during startup
 *
 * @deprecated make use of ExceptionMapper interface, ServiceDiscovery and dependency injection
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface ExceptionMapperProvider {
}
