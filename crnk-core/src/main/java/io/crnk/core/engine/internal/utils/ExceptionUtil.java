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
}
