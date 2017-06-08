package io.crnk.core.resource.meta;

import io.crnk.core.resource.list.ResourceList;

/**
 * Implement this class and provide the total number of (potentially filtered) resources
 * to let Crnk compute pagination links. The meta information can be delivered
 * as part of the result by returning an instance of {@link ResourceList}
 * <p>
 * <p>
 * Note that in case of the use of LinksInformation,
 * PagedLinksInformation must be implemented as well. Otherwise a default links implementation is used.
 * </p>
 * <p>
 * If the computation of the total resource count is deemed to expensive, you may want to consider
 * the use of {@link HasMoreResourcesMetaInformation}.
 * </p>
 */
public interface PagedMetaInformation extends MetaInformation {

	Long getTotalResourceCount();

	void setTotalResourceCount(Long totalResourceCount);
}
