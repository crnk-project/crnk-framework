package io.crnk.legacy.locator;

/**
 * Sample implementation of {@link JsonServiceLocator}. It makes new instance for every method call.
 *
 * @deprecated make use of ServiceDiscovery and ServiceDiscoveryFactory
 */
@Deprecated
public class SampleJsonServiceLocator implements JsonServiceLocator {
	@Override
	public <T> T getInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
