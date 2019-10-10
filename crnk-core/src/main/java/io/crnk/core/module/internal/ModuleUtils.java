package io.crnk.core.module.internal;

import io.crnk.core.engine.information.NamingStrategy;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.module.ModuleRegistry;

public class ModuleUtils {

	public static void adaptInformation(ResourceInformation information, ModuleRegistry moduleRegistry) {
		for (NamingStrategy namingStrategy : moduleRegistry.getNamingStrategies()) {
			information.setResourcePath(namingStrategy.adaptPath(information.getResourcePath()));
		}
	}

}
