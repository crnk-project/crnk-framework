package io.crnk.operations.internal;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class OperationParameterUtils {


	public static String parsePath(String url) {
		int sep = url.indexOf('?');
		return sep != -1 ? url.substring(0, sep) : url;
	}

	public static Map<String, Set<String>> parseParameters(String url) {
		int sep = url.indexOf('?');
		Map<String, Set<String>> parameters = new HashMap<>();
		if (sep != -1) {
			String query = url.substring(sep + 1);
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				try {
					int idx = pair.indexOf("=");
					String name = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
					String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");

					Set<String> values = parameters.get(name);
					if (values == null) {
						values = new LinkedHashSet<>();
						parameters.put(name, values);
					}
					values.add(value);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return parameters;
	}
}
