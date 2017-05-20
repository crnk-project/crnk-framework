package io.crnk.meta.provider;

import io.crnk.core.module.Module;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;

import java.util.Collection;

public interface MetaProviderContext {

	void add(MetaElement element);

	void addAll(Collection<? extends MetaElement> elements);

	MetaLookup getLookup();

	Module.ModuleContext getModuleContext();

}
