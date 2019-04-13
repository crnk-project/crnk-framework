package io.crnk.test.mock.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonApiResource(type = "schedule", resourcePath = "schedules")
public class Schedule {

    @JsonApiId
    private Long id;

    private String name;

    @JsonProperty("description")
    private String desc;

    @JsonApiRelation(mappedBy = "schedule")
    @JsonProperty("taskSet")
    private Set<Task> tasks;

    @JsonApiRelationId
    private Long projectId;

    @JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
    private Project project;

    @JsonApiRelationId
    private List<Long> projectIds = new ArrayList<>();

    @JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
    private List<Project> projects = new ArrayList<>();

    @JsonApiRelation(serialize = SerializeType.EAGER)
    private ScheduleStatus status;

    private boolean delayed;

    private Map<String, String> customData;

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public boolean isDelayed() {
        return delayed;
    }

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }

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


    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
        this.projects = null;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;

        if (projects != null) {
            List<Long> ids = new ArrayList<>();
            for (Project project : projects) {
                ids.add(project.getId());
            }
            this.projectIds = ids;
        } else {
            projectIds = null;
        }
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, String> getCustomData() {
        return customData;
    }

    public void setCustomData(Map<String, String> customData) {
        this.customData = customData;
    }
}
