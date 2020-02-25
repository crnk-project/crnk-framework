package io.crnk.test.mock.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiVersion;

// tag::docs[]
@JsonApiResource(type = "versionedTask")
@JsonApiVersion(max = 5)
public class VersionedTask {

    public enum CompletionStatus {
        DONE,
        OPEN,
        IN_PROGRESS
    }

    @JsonApiId
    private Long id;

    private String name;

    @JsonApiVersion(min = 1, max = 3)
    private boolean completed;

    @JsonApiVersion(min = 5)
    @JsonProperty("completed")
    private CompletionStatus newCompleted;

// end::docs[]

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public CompletionStatus getNewCompleted() {
        return newCompleted;
    }

    public void setNewCompleted(CompletionStatus newCompleted) {
        this.newCompleted = newCompleted;
    }

    // tag::docs[]
}
// end::docs[]