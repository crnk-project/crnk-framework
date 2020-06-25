package io.crnk.core.exception;

/**
 * General type for exceptions, which can be thrown during Crnk request processing.
 */
public abstract class CrnkException extends RuntimeException {

	private String url;

	public CrnkException(String message) {
		super(message);
	}

	public CrnkException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}