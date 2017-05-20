package io.crnk.meta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.resource.annotations.JsonApiResource;

import java.util.*;

@JsonApiResource(type = "meta/collectionType")
public abstract class MetaCollectionType extends MetaType {

	@JsonIgnore
	public <T> Collection<T> newInstance() {
		if (getImplementationClass() == Set.class)
			return new HashSet<>();
		if (getImplementationClass() == List.class)
			return new ArrayList<>();
		throw new UnsupportedOperationException(getImplementationClass().getName());
	}

}
