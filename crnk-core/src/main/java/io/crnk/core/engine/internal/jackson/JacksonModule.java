package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.module.Module;

public class JacksonModule implements Module {

	private static final String JSON_API_JACKSON_MODULE_NAME = "crnk";

	private final ObjectMapper objectMapper;

	public JacksonModule(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public String getModuleName() {
		return "jackson";
	}

	@Override
	public void setupModule(ModuleContext context) {
		objectMapper.registerModule(createJacksonModule());

		DefaultResourceFieldInformationProvider defaultFieldProvider = new DefaultResourceFieldInformationProvider();
		ResourceFieldInformationProvider jacksonFieldProvider = new JacksonResourceFieldInformationProvider();

		// TODO move somewhere else and make use of a SerializerExtension
		context.addResourceInformationBuilder(new DefaultResourceInformationProvider(defaultFieldProvider, jacksonFieldProvider));
	}


	/**
	 * Creates Crnk Jackson module with all required serializers
	 *
	 * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
	 */
	public static SimpleModule createJacksonModule() {
		SimpleModule simpleModule = new SimpleModule(JSON_API_JACKSON_MODULE_NAME,
				new Version(1, 0, 0, null, null, null));
		simpleModule.addSerializer(new ErrorDataSerializer());
		simpleModule.addDeserializer(ErrorData.class, new ErrorDataDeserializer());
		return simpleModule;
	}
}
