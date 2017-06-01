package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.registry.ResourceRegistry;

/**
 * Creates Jackson {@link com.fasterxml.jackson.databind.Module} with all custom Crnk serializers.
 */
public class JsonApiModuleBuilder {

	public static final String JSON_API_MODULE_NAME = "JsonApiModule";

	/**
	 * Creates Crnk Jackson module with all required serializers
	 *
	 * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
	 */
	public SimpleModule build() {
		SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME,
				new Version(1, 0, 0, null, null, null));

		simpleModule.addSerializer(new ErrorDataSerializer());
		simpleModule.addDeserializer(ErrorData.class, new ErrorDataDeserializer());

		return simpleModule;
	}
}
