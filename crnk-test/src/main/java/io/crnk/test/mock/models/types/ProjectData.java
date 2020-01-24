package io.crnk.test.mock.models.types;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectData {

	private String data;

	private String[] keywords;

	private Map<String, String> customData;

	@JsonProperty("due")
	private OffsetDateTime dueDate;

	private byte[] image;

	public String getData() {
		return data;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
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

	public OffsetDateTime getDueDate() {
		return dueDate;
	}

	public void setDueDate(OffsetDateTime dueDate) {
		this.dueDate = dueDate;
	}
}
