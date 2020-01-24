package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.links.LinksInformation;

import java.util.Collection;

/**
 * An optional interface that can be implemented along with {@link ResourceRepository},{@link
 * ManyRelationshipRepository}  or {@link
 * OneRelationshipRepository} to get links information about returned resource(s).
 * <p>
 * consisder the use ResourceList instead
 */
public interface LinksRepository<T> {

    /**
     * Return meta information about a resource. Can be called after find resource methods call
     *
     * @param resources a list of found resource(s)
     * @param querySpec sent along with the request
     * @param current   potential {@link LinksInformation} provided by {@link io.crnk.core.resource.list.ResourceList}
     * @return meta information object
     */
    LinksInformation getLinksInformation(Collection<T> resources, QuerySpec querySpec, LinksInformation current);
}
