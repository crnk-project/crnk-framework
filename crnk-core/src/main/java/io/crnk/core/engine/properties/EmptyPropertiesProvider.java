package io.crnk.core.engine.properties;

import io.crnk.core.engine.internal.utils.StringUtils;

/**
 * Just an empty properties provider. Always returns an empty String.
 */
@Deprecated
public class EmptyPropertiesProvider implements PropertiesProvider {

	@Override
	public String getProperty(String key) {
		return StringUtils.EMPTY;
	}
}
