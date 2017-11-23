package io.crnk.meta.internal.typed;

import io.crnk.core.module.Module;
import io.crnk.meta.model.MetaElement;

import java.lang.reflect.Type;

public interface TypedMetaElementFactoryContext {


	MetaElement allocate(Type type);

	Module.ModuleContext getModuleContext();

}
