package io.crnk.core.module.discovery;

import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Searches for an implementation of the ServiceDiscovery with java.util.ServiceLoader. Add e.g. crnk-cdi to your classpath
 * to pickup the CdiServiceDiscovery.
 */
public class DefaultServiceDiscoveryFactory implements ServiceDiscoveryFactory {

	@Override
	public ServiceDiscovery getInstance() {
		ServiceLoader<ServiceDiscovery> loader = ServiceLoader.load(ServiceDiscovery.class);
		Iterator<ServiceDiscovery> iterator = loader.iterator();
		if (iterator.hasNext()) {
			ServiceDiscovery discovery = iterator.next();
			PreconditionUtil.assertFalse("expected unique ServiceDiscovery implementation, got: " + loader, iterator.hasNext());
			return discovery;
		}
		return null;
	}
}
