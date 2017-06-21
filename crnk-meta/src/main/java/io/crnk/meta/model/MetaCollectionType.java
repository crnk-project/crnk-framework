package io.crnk.meta.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource("meta/collectionType")
public abstract class MetaCollectionType extends MetaType {

	@JsonIgnore
	public <T> Collection<T> newInstance() {
		if (getImplementationClass() == Set.class) {
			return new HashSet<>();
		}
		PreconditionUtil.assertEquals("expect Set or List type", List.class, getImplementationClass());
		return new ArrayList<>();
	}

}
