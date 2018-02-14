package io.crnk.meta.provider;

import io.crnk.core.utils.Optional;
import io.crnk.meta.model.MetaElement;

import java.lang.reflect.Type;

public interface MetaPartition {

	void init(MetaPartitionContext context);

	void discoverElements();

	Optional<MetaElement> allocateMetaElement(Type type);

	MetaElement getMeta(Type type);

	boolean hasMeta(Type type);
}
