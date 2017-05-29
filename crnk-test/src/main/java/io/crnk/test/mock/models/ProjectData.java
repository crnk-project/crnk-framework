package io.crnk.test.mock.models;

import java.util.Map;

public class ProjectData {

	private String data;

	private String[] keywords;

	private Map<String, String> customData;

	public String getData() {
		return data;
	}

	public ProjectData setData(@SuppressWarnings("SameParameterValue") String data) {
		this.data = data;
		return this;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public Map<String, String> getCustomData() {
		return customData;
	}

	public void setCustomData(Map<String, String> customData) {
		this.customData = customData;
	}
}
