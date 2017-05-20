package io.crnk.core.engine.internal.information.resource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.utils.Optional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Classes which implement those interface are able to provide a set of resource attributes
 */
public class ResourceAttributesBridge<T> {

	private final List<ResourceField> staticFields;
	private final Class<T> resourceClass;
	private Method jsonAnyGetter;
	private Method jsonAnySetter;

	public ResourceAttributesBridge(List<ResourceField> staticFields, Class<T> resourceClass) {
		this.staticFields = staticFields;
		this.resourceClass = resourceClass;

		initializeGetterAndSetter(resourceClass);
	}

	private void initializeGetterAndSetter(Class<T> resourceClass) {
		this.jsonAnyGetter = ClassUtils.findMethodWith(resourceClass, JsonAnyGetter.class);
		this.jsonAnySetter = ClassUtils.findMethodWith(resourceClass, JsonAnySetter.class);

		if (absentAnySetter()) {
			throw new InvalidResourceException(
					String.format("A resource %s has to have both methods annotated with @JsonAnySetter and @JsonAnyGetter",
							resourceClass.getCanonicalName()));
		}
	}

	/**
	 * The resource has to have both method annotated with {@link JsonAnySetter} and {@link JsonAnyGetter} to allow
	 * proper handling.
	 *
	 * @return <i>true</i> if resource definition is incomplete, <i>false</i> otherwise
	 */
	private boolean absentAnySetter() {
		return (jsonAnySetter == null && jsonAnyGetter != null) ||
				(jsonAnySetter != null && jsonAnyGetter == null);
	}

	public void setProperty(ObjectMapper objectMapper, T instance, JsonNode valueNode, String propertyName) {
		Optional<ResourceField> optStaticField = findStaticField(propertyName);
		try {
			if (optStaticField.isPresent()) {
				ResourceField field = optStaticField.get();
				Type valueType = field.getGenericType();
				Object value;
				if (valueNode != null) {
					JavaType jacksonValueType = objectMapper.getTypeFactory().constructType(valueType);
					ObjectReader reader = objectMapper.reader().forType(jacksonValueType);
					value = reader.readValue(valueNode);
				} else {
					value = null;
				}
				field.getAccessor().setValue(instance, value);
			} else if (jsonAnySetter != null) {
				// Needed for JsonIgnore and dynamic attributes
				Object value = objectMapper.reader().forType(Object.class).readValue(valueNode);
				jsonAnySetter.invoke(instance, propertyName, value);
			}
		} catch (IOException | IllegalAccessException | InvocationTargetException e) {
			throw new ResourceException(
					String.format("Exception while reading %s.%s=%s due to %s", instance, propertyName, valueNode, e.getMessage()), e);
		}
	}

	private Optional<ResourceField> findStaticField(String propertyName) {
		for (ResourceField resourceField : staticFields) {
			if (resourceField.getJsonName().equals(propertyName)) {
				return Optional.of(resourceField);
			}
		}
		return Optional.empty();
	}

	public List<ResourceField> getFields() {
		return staticFields;
	}
}
