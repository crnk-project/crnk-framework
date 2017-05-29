package io.crnk.core.engine.properties;

/**
 * Just an empty properties provider. Always returns a null.
 */
public class NullPropertiesProvider implements PropertiesProvider {

	@Override
	public String getProperty(String key) {
		return null;
	}
}
