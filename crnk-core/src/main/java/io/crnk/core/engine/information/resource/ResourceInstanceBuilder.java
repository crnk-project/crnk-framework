package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;

/**
 * Used to construct an object instance for the requested resource. {@link DefaultResourceInstanceBuilder} just
 * creates new empty object instances using the default constructor. More elaborate instances may do more,
 * like binding created entity instances to a JPA session.
 */
public interface ResourceInstanceBuilder<T> {

	/**
	 * @param body request body
	 * @return resource object
	 */
	T buildResource(Resource body);
}