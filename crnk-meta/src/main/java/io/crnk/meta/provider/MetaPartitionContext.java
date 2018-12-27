package io.crnk.meta.provider;

import io.crnk.core.module.Module;
import io.crnk.meta.model.MetaElement;

import java.util.Optional;
import java.util.concurrent.Callable;

public interface MetaPartitionContext {

	void addElement(MetaElement element);

	Module.ModuleContext getModuleContext();

	Optional<MetaElement> getMetaElement(String id);

	MetaPartition getBasePartition();

	<T> T runDiscovery(Callable<T> callable);
}
