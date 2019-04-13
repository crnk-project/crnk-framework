package io.crnk.core.resource.paging.next;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "tasks")
@JsonPropertyOrder(alphabetic = true)
public class HasNextPageResource {

    @JsonApiId
    private Long id;

    private String name;

    private String category;

    private boolean completed;

    private boolean deleted;

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }


    public Long getId() {
        return id;
    }

    public HasNextPageResource setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(@SuppressWarnings("SameParameterValue") String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be set to null!");
        }
        this.category = category;
    }

}
