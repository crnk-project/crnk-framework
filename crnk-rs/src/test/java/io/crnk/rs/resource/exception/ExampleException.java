package io.crnk.rs.resource.exception;

public class ExampleException extends RuntimeException {

	public static final String ERROR_ID = "testId";
	public static final String ERROR_TITLE = "errorTitle";

	private final String id;
	private final String title;

	public ExampleException(String id, String title) {
		this.id = id;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
}
