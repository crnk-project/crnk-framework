package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "specialTask", resourcePath = "superTasks")
public class SpecialTask extends SuperTask {

	private boolean recurring;

	private String end;

	public boolean isRecurring() {
		return recurring;
	}

	public void setRecurring(final boolean recurring) {
		this.recurring = recurring;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(final String end) {
		this.end = end;
	}
}