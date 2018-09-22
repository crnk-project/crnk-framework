package io.crnk.core.engine.internal.utils;

import java.util.concurrent.Callable;

public class ExceptionUtil {

	private ExceptionUtil() {
		// util
	}


	/**
	 * Use this method for exceptiosn that are "never" happen, like missing UTF8 encoding.
	 * This method also will make sure no coverage gets lost for such cases.
	 */
	public static <T> T wrapCatchedExceptions(Callable<T> callable) {
		try {
			return callable.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Use this method for exceptiosn that are "never" happen, like missing UTF8 encoding.
	 * This method also will make sure no coverage gets lost for such cases.
	 * <p>
	 * Allows to specify a formatted message with optional parameters.
	 */
	public static <T> T wrapCatchedExceptions(Callable<T> callable, String messageFormat, Object... params) {
		try {
			return callable.call();
		} catch (Exception e) {
			String formattedMessage = String.format(messageFormat, params);
			throw new IllegalStateException(formattedMessage, e);
		}
	}
}
