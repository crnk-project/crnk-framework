package io.crnk.meta.internal;

import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaProvider;

import java.util.List;

public class MetaUtils {

	private MetaUtils() {

	}

	public static final String firstToLower(String value) {
		if (value.length() > 0) {
			return Character.toLowerCase(value.charAt(0)) + value.substring(1);
		}
		return value;
	}


	public static MetaElement adjustForRequest(MetaLookup lookup, MetaElement element) {
		List<MetaProvider> providers = lookup.getProviders();
		for (MetaProvider provider : providers) {
			element = provider.adjustForRequest(element);
			if (element == null) {
				break;
			}
		}
		return element;
	}
}
