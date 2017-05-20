package io.crnk.meta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;

@JsonApiResource(type = "meta/type")
public class MetaType extends MetaElement {

	@JsonIgnore
	private Type implementationType;

	@JsonApiRelation(serialize = SerializeType.LAZY)
	private MetaType elementType;

	@JsonIgnore
	public Class<?> getImplementationClass() {
		return ClassUtils.getRawType(implementationType);
	}

	public Type getImplementationType() {
		return implementationType;
	}

	public void setImplementationType(Type implementationType) {
		if (implementationType == long.class) {
			throw new IllegalArgumentException();
		}
		this.implementationType = implementationType;
	}

	@JsonIgnore
	public boolean isCollection() {
		return this instanceof MetaCollectionType;
	}

	@JsonIgnore
	public MetaCollectionType asCollection() {
		return (MetaCollectionType) this;
	}

	@JsonIgnore
	public boolean isMap() {
		return this instanceof MetaMapType;
	}

	@JsonIgnore
	public MetaMapType asMap() {
		return (MetaMapType) this;
	}

	public MetaType getElementType() {
		if (elementType == null) {
			throw new IllegalStateException(getClass().getName());
		}
		return elementType;
	}

	public void setElementType(MetaType elementType) {
		if (elementType == null) {
			throw new NullPointerException();
		}
		this.elementType = elementType;
	}
}
