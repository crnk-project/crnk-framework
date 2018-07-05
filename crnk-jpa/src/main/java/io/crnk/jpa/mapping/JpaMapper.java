package io.crnk.jpa.mapping;

import io.crnk.core.queryspec.QuerySpec;
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
	 * proper inserts, updates and deletes. In case of an update, an implementation must to lookup the entity
	 * with the entity manager to obtain a managed instance. In case of a create, it can simple create a new instance.
	 *
	 * @param dto to map to an entity.
	 * @return entity
	 */
	E unmap(D dto);


	/**
	 * Maps a DTO-based QuerySpec to a Entity-based QuerySpec. The method can do arbitrary changes, like mapping attribute names
	 * or changing type, such as translating Enums.
	 *
	 * @return mapped QuerySpec
	 */
	default QuerySpec unmapQuerySpec(QuerySpec dtoQueryspec) {
		return dtoQueryspec;
	}
}
