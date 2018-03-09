package io.crnk.reactive.repository;

import java.lang.annotation.*;

/**
 * Allows to mark a non-reactive repository as being immediate/non-blocking. Calls will then not be moved to worker threads.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ImmediateRepository {
}
