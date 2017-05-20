package io.crnk.jpa.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows a JPA entity to include related entities in the exposed JSON API resource.
 * For a consumer it looks like a single (large) resource that can be inserted, updated
 * and deleted with a single request and, as such, in a single transaction.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface JpaMergeRelations {

	/**
	 * Defines set of relationship attributes that should be merged
	 * into this document and treated as regular attribute of this document.
	 */
	String[] attributes();
}
