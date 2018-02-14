package io.crnk.meta.internal.typed;

import io.crnk.core.module.Module;
import io.crnk.core.utils.Optional;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaPartitionBase;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class TypedMetaPartitionBase extends MetaPartitionBase {


	private List<TypedMetaElementFactory> factories = new CopyOnWriteArrayList<>();


	protected void addFactory(final TypedMetaElementFactory factory) {
		if (!factories.contains(factory)) {
			factory.init(new TypedMetaElementFactoryContext() {
				@Override
				public MetaElement allocate(Type type) {
					return TypedMetaPartitionBase.this.allocateMetaElement(type).get();
				}

				@Override
				public Module.ModuleContext getModuleContext() {
					return context.getModuleContext();
				}
			});
			factories.add(factory);
		}
	}

	@Override
	protected Optional<MetaElement> doAllocateMetaElement(Type type) {
		for (TypedMetaElementFactory factory : factories) {
			if (factory.accept(type)) {
				MetaElement element = factory.create(type);
				addElement(type, element);
				return Optional.of(element);
			}
		}
		return Optional.empty();
	}

}
