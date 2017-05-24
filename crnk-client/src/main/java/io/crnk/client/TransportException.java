package io.crnk.client;

/**
 * General exception when the underyling connection was causing an exception.
 */
public class TransportException extends RuntimeException {

	public TransportException(Throwable cause) {
		super(cause);
	}
}
