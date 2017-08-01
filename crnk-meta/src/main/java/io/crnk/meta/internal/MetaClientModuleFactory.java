package io.crnk.meta.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.meta.MetaModule;

public class MetaClientModuleFactory implements ClientModuleFactory {

	@Override
	public MetaModule create() {
		return MetaModule.create();
	}
}