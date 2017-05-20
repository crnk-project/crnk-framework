package io.crnk.core.resource.list;

import io.crnk.core.engine.internal.utils.WrappedList;
import io.crnk.core.resource.meta.PagedMetaInformation;

import java.util.List;

/**
 * Use this class as return type and provide the total number of (potentially filtered)
 * to let Crnk compute pagination links. Note that in case of the use of LinksInformation,
 * PagedLinksInformation must be implemented. Otherwise a default implementation is used.
 *
 * @Deprecated It is recommended to to implement {@link PagedMetaInformation} instead and use in combination with {@link ResourceList}.
 */
@Deprecated
public class PagedResultList<T> extends WrappedList<T> {

	private Long totalCount;

	public PagedResultList(List<T> list, Long totalCount) {
		super(list);
		this.totalCount = totalCount;
	}

	public Long getTotalCount() {
		return totalCount;
	}
}
