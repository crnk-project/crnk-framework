package io.crnk.core.engine.internal.utils;

public class CompareUtils {

	private CompareUtils() {
	}

	public static boolean isEquals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
}
