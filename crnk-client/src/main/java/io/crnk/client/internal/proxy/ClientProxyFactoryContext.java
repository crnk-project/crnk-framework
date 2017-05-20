package io.crnk.client.internal.proxy;

import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.resource.list.DefaultResourceList;

public interface ClientProxyFactoryContext {

	ModuleRegistry getModuleRegistry();

	<T> DefaultResourceList<T> getCollection(Class<T> resourceClass, String url);

}
