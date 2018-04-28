package io.crnk.core.mock.models;

import java.util.HashMap;
import java.util.Map;

public class ProjectData {
	private String data;

	private Map<String, Integer> priorities = new HashMap<>();

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
