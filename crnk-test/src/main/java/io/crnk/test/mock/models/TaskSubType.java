package io.crnk.test.mock.models;

import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource("tasksSubType")
public class TaskSubType extends Task {

	private int subTypeValue;

	public int getSubTypeValue() {
		return subTypeValue;
	}

	public void setSubTypeValue(int subTypeValue) {
		this.subTypeValue = subTypeValue;
	}
}
