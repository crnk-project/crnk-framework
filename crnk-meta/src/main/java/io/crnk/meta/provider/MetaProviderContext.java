package io.crnk.meta.provider;

import io.crnk.core.module.Module;
import io.crnk.core.utils.Optional;
import io.crnk.meta.model.MetaElement;

import java.util.concurrent.Callable;

public interface MetaProviderContext {

	Module.ModuleContext getModuleContext();

	Optional<MetaElement> getMetaElement(String id);

	void checkInitialized();

	<T> T runDiscovery(Callable<T> callable);
}
