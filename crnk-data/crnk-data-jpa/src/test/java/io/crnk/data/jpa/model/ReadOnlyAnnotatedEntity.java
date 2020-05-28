package io.crnk.data.jpa.model;

import io.crnk.core.resource.annotations.JsonApiResource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@JsonApiResource(postable = false, patchable = false, deletable = false, type = "readOnlyAnnotated")
public class ReadOnlyAnnotatedEntity {

    @Id
    private String id;

    @Column(name = "ctl_act_cd")
    private Boolean value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
