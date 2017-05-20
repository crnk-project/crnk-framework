package io.crnk.jpa.mapping;

import io.crnk.jpa.query.Tuple;

/**
 * Maps a tuple to a DTO and a DTO back to a entity.
 */
public interface JpaMapper<E, D> {

	/**
	 * Usually the first tuple entry is the entity and any additonal tuple entries
	 * are computed attributes. However, applications may choose to override this
	 * to only fetch a subset of attributes for performance reasons.
	 *
	 * @param tuple to map to a DTO. Usually the first entry is the entity. Additional entries are compuated attributes.
	 * @return mapped dto
	 */
	D map(Tuple tuple);

	/**
	 * Maps the dto back to the entity. Make sure to return a managed entity instance to support
	 * proper inserts, updates and deletes. An implementation may choose to lookup the entity
	 * with the entity manager and create a new instance if it has not been found.
	 *
	 * @param dto to map to an entity.
	 * @return entity
	 */
	E unmap(D dto);

}
