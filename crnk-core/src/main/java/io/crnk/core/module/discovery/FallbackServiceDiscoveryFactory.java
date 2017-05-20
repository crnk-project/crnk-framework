package io.crnk.core.module.discovery;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.legacy.locator.JsonServiceLocator;

public class FallbackServiceDiscoveryFactory implements ServiceDiscoveryFactory {

	ServiceDiscoveryFactory factory;

	JsonServiceLocator serviceLocator;

	PropertiesProvider propertiesProvider;

	public FallbackServiceDiscoveryFactory(ServiceDiscoveryFactory factory, JsonServiceLocator serviceLocator,
										   PropertiesProvider propertiesProvider) {
		this.factory = factory;
		this.serviceLocator = serviceLocator;
		this.propertiesProvider = propertiesProvider;
	}

	@Override
	public ServiceDiscovery getInstance() {
		ServiceDiscovery instance = factory.getInstance();
		if (instance != null) {
			return instance;
		}
		String resourceSearchPackage = propertiesProvider.getProperty(CrnkProperties.RESOURCE_SEARCH_PACKAGE);
		if (resourceSearchPackage != null) {
			return new ReflectionsServiceDiscovery(resourceSearchPackage, serviceLocator);
		}
		return new EmptyServiceDiscovery();
	}

}
