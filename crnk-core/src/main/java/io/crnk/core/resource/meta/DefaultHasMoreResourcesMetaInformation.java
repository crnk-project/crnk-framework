package io.crnk.core.resource.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Default implementation for {@link HasMoreResourcesMetaInformation}. Note that unlike
 * the totalResourceCount this information here is not transported to the client (pagination itself is sufficient).
 */
public class DefaultHasMoreResourcesMetaInformation implements HasMoreResourcesMetaInformation {

	/**
	 * No need to serialize this, pagination sufficient
	 */
	@JsonIgnore
	private Boolean hasMoreResources;

	@Override
	public Boolean getHasMoreResources() {
		return hasMoreResources;
	}

	public void setHasMoreResources(Boolean hasMoreResources) {
		this.hasMoreResources = hasMoreResources;
	}
}
