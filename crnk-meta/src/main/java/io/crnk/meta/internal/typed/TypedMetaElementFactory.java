package io.crnk.meta.internal.typed;

import io.crnk.meta.model.MetaElement;

import java.lang.reflect.Type;

public interface TypedMetaElementFactory {

	void init(TypedMetaElementFactoryContext context);

	boolean accept(Type type);

	MetaElement create(Type type);
}



