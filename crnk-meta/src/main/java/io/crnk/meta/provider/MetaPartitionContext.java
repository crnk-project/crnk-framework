package io.crnk.meta.provider;

import io.crnk.core.module.Module;
import io.crnk.core.utils.Optional;
import io.crnk.meta.model.MetaElement;

public interface MetaPartitionContext {

	void addElement(MetaElement element);

	Module.ModuleContext getModuleContext();

	Optional<MetaElement> getMetaElement(String id);

	MetaPartition getBasePartition();
}
