package io.crnk.meta.internal;

public class MetaUtils {

	private MetaUtils() {

	}

	public static final String firstToLower(String value) {
		if (value.length() > 0) {
			return Character.toLowerCase(value.charAt(0)) + value.substring(1);
		}
		return value;
	}
}
