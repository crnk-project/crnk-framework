package io.crnk.validation.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.validation.mock.ComplexValid;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@JsonApiResource("projects")
@ComplexValid
public class Project {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	private String description;

	@Valid
	private ProjectData data;

	@Valid
	private List<ProjectData> dataList = new ArrayList<>();

	@Valid
	private Set<ProjectData> dataSet = new HashSet<>();

	@Valid
	private Map<String, ProjectData> dataMap = new HashMap<>();

	@Size(min = 0, max = 3)
	private List<String> keywords = new ArrayList<>();

	@NotNull
	private Map<String, String> attributes = new HashMap<>();

	@JsonApiToMany
	@Valid
	private List<Task> tasks = new ArrayList<>();

	@JsonApiToMany
	@Valid
	private Task task;

	public Long getId() {
		return id;
	}

	public Project setId(Long id) {
		this.id = id;
		return this;
	}

	public List<ProjectData> getDataList() {
		return dataList;
	}

	public void setDataList(List<ProjectData> dataList) {
		this.dataList = dataList;
	}

	public Map<String, ProjectData> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, ProjectData> dataMap) {
		this.dataMap = dataMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(@SuppressWarnings("SameParameterValue") String description) {
		this.description = description;
	}

	public ProjectData getData() {
		return data;
	}

	public void setData(ProjectData data) {
		this.data = data;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Set<ProjectData> getDataSet() {
		return dataSet;
	}

	public void setDataSet(Set<ProjectData> dataSet) {
		this.dataSet = dataSet;
	}

}
