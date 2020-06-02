package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;

import java.util.Optional;

/**
 * Holds logic of retrieving property name based on {@link JsonProperty} value
 */
public class JacksonPropertyNameResolver {

	/**
	 * Retrieves name of the property
	 *
	 * @param attributeDesc Attribute information
	 * @return Optional name or empty
	 */
	public Optional<String> getJsonName(BeanAttributeInformation attributeDesc) {
		Optional<JsonProperty> ignoreAnnotation = attributeDesc.getAnnotation(JsonProperty.class);
		if (ignoreAnnotation.isPresent() && !ignoreAnnotation.get().value().isEmpty()) {
			return Optional.of(ignoreAnnotation.get().value());
		}

		return Optional.empty();
	}
}
