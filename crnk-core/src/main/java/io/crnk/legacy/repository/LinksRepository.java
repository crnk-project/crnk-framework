package io.crnk.legacy.repository;

import io.crnk.core.resource.links.LinksInformation;
import io.crnk.legacy.queryParams.QueryParams;

/**
 * An optional interface that can be implemented along with
 * {@link ResourceRepository} or {@link RelationshipRepository} to get links
 * information about returned document(s).
 *
 * @deprecated Make use of LinksRepositoryV2 or ResourceList
 */
@Deprecated
public interface LinksRepository<T> {
	/**
	 * Return meta information about a document. Can be called after find
	 * document methods call
	 * <p>
	 * <b>Consider the use of ResourceList instead.</b>
	 *
	 * @param resources   a list of found document(s)
	 * @param queryParams parameters sent along with the request
	 * @return meta information object
	 */
	LinksInformation getLinksInformation(Iterable<T> resources, QueryParams queryParams);
}
