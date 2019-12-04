package io.crnk.data.jpa.model;

import io.crnk.core.resource.annotations.JsonApiResource;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@JsonApiResource(type = "renamedResource")
public class RenamedTestEntity {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
