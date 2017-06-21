package io.crnk.meta.model;

import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource("meta/type")
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
		PreconditionUtil.assertNotNull(getClass().getName(), elementType);
		return elementType;
	}

	public void setElementType(MetaType elementType) {
		PreconditionUtil.assertNotNull("cannot be null", elementType);
		this.elementType = elementType;
	}
}
