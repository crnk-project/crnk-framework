package io.crnk.security.repository;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "securityRole", resourcePath = "security/role")
public class Role {

    @JsonApiId
    private String id;

    public Role() {
    }

    public Role(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
