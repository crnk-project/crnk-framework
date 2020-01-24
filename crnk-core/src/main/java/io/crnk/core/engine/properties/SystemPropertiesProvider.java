package io.crnk.core.engine.properties;

public class SystemPropertiesProvider implements PropertiesProvider {

	@Override
	public String getProperty(String key) {
		return System.getProperty(key);
	}
}