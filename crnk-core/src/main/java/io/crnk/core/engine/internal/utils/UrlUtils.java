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

	public static String addTrailingSlash(String url) {
		if (url != null && !url.endsWith("/")) {
			return url + "/";
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

	public static String addLeadingSlash(String url) {
		if (url != null && !url.startsWith("/")) {
			return "/" + url;
		} else {
			return url;
		}
	}

	public static String concat(String baseUrl, String... paths) {
		StringBuilder builder = new StringBuilder();
		builder.append(removeTrailingSlash(baseUrl));
		for (String path : paths) {
			builder.append('/');
			builder.append(removeLeadingSlash(path));
		}
		return builder.toString();
	}

	public static String appendRelativePath(String baseUrl, String relativePath) {
		int sep = baseUrl.indexOf("?");
		if (sep != -1) {
			return baseUrl.substring(0, sep) + relativePath + baseUrl.substring(sep);
		}
		return baseUrl + relativePath;
	}
}
