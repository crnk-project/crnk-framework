package io.crnk.meta.model.resource;

import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/**
 * A JSON:API resource.
 */
@JsonApiResource(type = "metaResource", resourcePath = "meta/resource")
public class MetaResource extends MetaResourceBase { // NOSONAR ignore exception hierarchy

	private String resourceType;

	private String resourcePath;

	@JsonApiRelation
	private MetaResourceRepository repository;

	private VersionRange versionRange;

	public VersionRange getVersionRange() {
		return versionRange;
	}

	public void setVersionRange(VersionRange versionRange) {
		this.versionRange = versionRange;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public MetaResourceRepository getRepository() {
		return repository;
	}

	public void setRepository(MetaResourceRepository repository) {
		this.repository = repository;
	}
}
