package io.crnk.legacy.locator;

/**
 * Central interface to provide domain repositories to the framework. Each document is fetched from implementation
 * of this locator.
 *
 * @deprecated make use of ServiceDiscovery and ServiceDiscoveryFactory
 */
@Deprecated
public interface JsonServiceLocator {

	/**
	 * Get an instance of a class
	 *
	 * @param clazz class to be searched for
	 * @param <T>   type of returning object
	 * @return instance of a class of type T which implements/extends or is an instance of clazz
	 */
	<T> T getInstance(Class<T> clazz);
}
