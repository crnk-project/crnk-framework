package io.crnk.core.engine.internal.utils;

public class UrlUtils {

	private UrlUtils() {
	}

	public static String removeTrailingSlash(String url) {
		if (url != null && url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		} else {
			return url;
		}
	}

	public static String removeLeadingSlash(String url) {
		if (url != null && url.startsWith("/")) {
			return url.substring(1);
		} else {
			return url;
		}
	}
}
