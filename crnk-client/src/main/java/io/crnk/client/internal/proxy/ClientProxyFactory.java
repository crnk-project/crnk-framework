package io.crnk.client.internal.proxy;

import java.util.Collection;

/**
 * Used to create stubs for resources, collections and interfaces.
 */
public interface ClientProxyFactory {

	void init(ClientProxyFactoryContext context);

	<T> T createResourceProxy(Class<T> clazz, Object id);

	<C extends Collection<T>, T> C createCollectionProxy(Class<T> resourceClass, Class<C> collectionClass, String url);
}
