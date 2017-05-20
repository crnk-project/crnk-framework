package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.ResourceRepository;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository} or {@link
 * RelationshipRepository} to get links information about returned resource(s).
 * <p>
 * consisder the use ResourceList instead
 */
public interface LinksRepositoryV2<T> {

	/**
	 * Return meta information about a resource. Can be called after find resource methods call
	 *
	 * @param resources a list of found resource(s)
	 * @param querySpec sent along with the request
	 * @return meta information object
	 */
	LinksInformation getLinksInformation(Iterable<T> resources, QuerySpec querySpec);
}
