package io.crnk.core.resource.meta;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DefaultPagedMetaInformation implements PagedMetaInformation {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long totalResourceCount;

	@Override
	public Long getTotalResourceCount() {
		return totalResourceCount;
	}

	@Override
	public void setTotalResourceCount(Long totalResourceCount) {
		this.totalResourceCount = totalResourceCount;
	}
}
