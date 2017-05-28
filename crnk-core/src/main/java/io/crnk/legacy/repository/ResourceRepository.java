package io.crnk.legacy.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.Repository;
import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;

/**
 * Base document which is used to operate on the resources. Each document should have a corresponding document
 * implementation.
 *
 * @param <T>  Type of an entity
 * @param <ID> Type of Identifier of an entity
 * @deprecated Make use of ResourceRepositoryV2
 */
@Deprecated
public interface ResourceRepository<T, ID extends Serializable> extends Repository {

	/**
	 * Search one document with a given ID. If a document cannot be found, a {@link ResourceNotFoundException}
	 * exception should be thrown.
	 *
	 * @param id          an identifier of the document
	 * @param queryParams parameters sent along with the request
	 * @return an instance of the document
	 */
	T findOne(ID id, QueryParams queryParams);

	/**
	 * Search for all of the resources. An instance of {@link QueryParams} can be used if necessary. If no
	 * resources can be found, an empty {@link Iterable} or <i>null</i> must be returned.
	 *
	 * @param queryParams parameters send with the request
	 * @return a list of found resources
	 */
	Iterable<T> findAll(QueryParams queryParams);

	/**
	 * Search for resources constrained by a list of identifiers. An instance of {@link QueryParams} can be used if
	 * necessary. If no resources can be found, an empty {@link Iterable} or <i>null</i> must be returned.
	 *
	 * @param ids         an {@link Iterable} of passed document identifiers
	 * @param queryParams parameters send with the request
	 * @return a list of found resources
	 */
	Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams);

	/**
	 * Saves a document. A Returning document must include assigned identifier created for the instance of document.
	 *
	 * @param entity document to be saved
	 * @param <S>    type of the document
	 * @return saved document. Must include set identifier.
	 */
	<S extends T> S save(S entity);

	/**
	 * Removes a document identified by id legacy.
	 *
	 * @param id identified of the document to be removed
	 */
	void delete(ID id);
}
