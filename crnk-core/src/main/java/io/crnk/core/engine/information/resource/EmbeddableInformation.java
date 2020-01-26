package io.crnk.core.engine.information.resource;

import io.crnk.core.exception.InvalidResourceException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * Represents the model of a type annotated with {@link io.crnk.core.resource.annotations.JsonApiEmbeddable}.
 */
public class EmbeddableInformation extends BeanInformationBase {


	public EmbeddableInformation(Type implementationType, List<ResourceField> fields) {
		super(implementationType, fields);
		initFields();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResourceInformation that = (ResourceInformation) o;
		return Objects.equals(implementationClass, that.implementationClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(implementationClass);
	}

	protected void initField(ResourceField field) {
		super.initField(field);
		field.setResourceInformation(this);
	}
}
