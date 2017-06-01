package io.crnk.meta.provider;

import io.crnk.core.module.Module;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;

public interface MetaProviderContext {

	void add(MetaElement element);

	MetaLookup getLookup();

	Module.ModuleContext getModuleContext();

}
