package io.crnk.data.jpa.model;

import io.crnk.core.resource.annotations.JsonApiResource;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@JsonApiResource(type = "jpaResourceTestEntity", resourcePath = "jpa-resource-test-entity")
public class JpaResourcePathTestEntity {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
