package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;

/**
 * A JSON API resource.
 */
@JsonApiResource(type = "meta/resource")
public class MetaResource extends MetaResourceBase { // NOSONAR ignore exception hierarchy

    private String resourceType;

    private String resourcePath;

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
}
