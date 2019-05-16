package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.legacy.repository.LegacyRelationshipRepository;
import io.crnk.legacy.repository.LegacyResourceRepository;

import java.util.Collection;

/**
 * An optional interface that can be implemented along with {@link LegacyResourceRepository} or {@link
 * LegacyRelationshipRepository} to get links information about returned resource(s).
 * <p>
 * consisder the use ResourceList instead
 */
public interface LinksRepository<T> {

	/**
	 * Return meta information about a resource. Can be called after find resource methods call
	 *
	 * @param resources a list of found resource(s)
	 * @param querySpec sent along with the request
	 * @return meta information object
	 */
	LinksInformation getLinksInformation(Collection<T> resources, QuerySpec querySpec);
}
