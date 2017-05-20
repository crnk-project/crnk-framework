package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.ResourceRepository;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get meta information about returned resource(s).
 * <p>
 * Consider the use use ResourceList instead
 */
public interface MetaRepositoryV2<T> {

	/**
	 * Return meta information about a resource. Can be called after find resource methods call
	 *
	 * @param resources a list of found resource(s)
	 * @param querySpec sent along with the request
	 * @return meta information object
	 */
	MetaInformation getMetaInformation(Iterable<T> resources, QuerySpec querySpec);
}
