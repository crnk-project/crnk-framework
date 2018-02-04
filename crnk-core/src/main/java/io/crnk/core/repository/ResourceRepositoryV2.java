package io.crnk.core.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;

/**
 * Base repository which is used to operate on the resources. Each resource should have a corresponding resource
 * implementation.
 *
 * @param <T> Type of an entity
 * @param <I> Type of Identifier of an entity
 * @param <L> List type
 */
public interface ResourceRepositoryV2<T, I extends Serializable> extends Repository {

	/**
	 * @return the class returned by this resource
	 */
	Class<T> getResourceClass();

	/**
	 * Search one resource with a given ID. If a resource cannot be found, a {@link ResourceNotFoundException}
	 * exception should be thrown.
	 *
	 * @param id        an identifier of the resource
	 * @param querySpec querySpec sent along with the request as parameters
	 * @return an instance of the resource
	 */
	T findOne(I id, QuerySpec querySpec);

	/**
	 * Search for all of the resources. An instance of {@link QueryParams} can be used if necessary. If no
	 * resources can be found, an empty {@link Iterable} or <i>null</i> must be returned.
	 *
	 * @param querySpec querySpec sent along with the request as parameters
	 * @return a list of found resources
	 */
	ResourceList<T> findAll(QuerySpec querySpec);

	/**
	 * Search for resources constrained by a list of identifiers. An instance of {@link QueryParams} can be used if
	 * necessary. If no resources can be found, an empty {@link Iterable} or <i>null</i> must be returned.
	 *
	 * @param ids       an {@link Iterable} of passed resource identifiers
	 * @param querySpec querySpec sent along with the request as parameters
	 * @return a list of found resources
	 */
	ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec);

	/**
	 * Saves a resource. A Returning resource must include assigned identifier created for the instance of resource.
	 *
	 * @param resource resource to be saved
	 * @param <S>    type of the resource
	 * @return saved resource. Must include set identifier.
	 */
	<S extends T> S save(S resource);

	/**
	 * Creates a resource. A Returning resource must include assigned identifier created for the instance of resource.
	 *
	 * @param resource resource to be saved
	 * @param <S>    type of the resource
	 * @return saved resource. Must include set identifier.
	 */
	<S extends T> S create(S resource);

	/**
	 * Removes a resource identified by id legacy.
	 *
	 * @param id identified of the resource to be removed
	 */
	void delete(I id);

}
