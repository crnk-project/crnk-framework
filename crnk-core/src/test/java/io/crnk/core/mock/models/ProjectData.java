package io.crnk.core.mock.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiEmbeddable;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonApiEmbeddable
public class ProjectData {
    private String data;

    private Map<String, Integer> priorities = new HashMap<>();

    private ProjectStatus status = new ProjectStatus();

    private List<String> keywords;

    @JsonProperty("due")
    private OffsetDateTime dueDate;

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public ProjectData setData(@SuppressWarnings("SameParameterValue") String data) {
        this.data = data;
        return this;
    }

    public Map<String, Integer> getPriorities() {
        return priorities;
    }

    public void setPriorities(Map<String, Integer> priorities) {
        this.priorities = priorities;
    }
}
