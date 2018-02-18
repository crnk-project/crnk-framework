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

	private final boolean serializeLinksAsObjects;

	public JacksonModule(ObjectMapper objectMapper) {
		this(objectMapper, false);
	}

	public JacksonModule(ObjectMapper objectMapper, boolean serializeLinksAsObjects) {
		this.objectMapper = objectMapper;
		this.serializeLinksAsObjects = serializeLinksAsObjects;
	}

	@Override
	public String getModuleName() {
		return "jackson";
	}

	@Override
	public void setupModule(ModuleContext context) {
		objectMapper.registerModule(createJacksonModule(serializeLinksAsObjects));

		DefaultResourceFieldInformationProvider defaultFieldProvider = new DefaultResourceFieldInformationProvider();
		ResourceFieldInformationProvider jacksonFieldProvider = new JacksonResourceFieldInformationProvider();

		// TODO move somewhere else and make use of a SerializerExtension
		context.addResourceInformationBuilder(new DefaultResourceInformationProvider(
			context.getPropertiesProvider(),
			defaultFieldProvider,
			jacksonFieldProvider));
	}


	/**
	 * Creates Crnk Jackson module with all required serializers
	 *
	 * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
	 */
	public static SimpleModule createJacksonModule() {
		return createJacksonModule(false);
	}

	/**
	 * Creates Crnk Jackson module with all required serializers.<br />
	 * Adds the {@link LinksInformationSerializer} if <code>serializeLinksAsObjects</code> is set to <code>true</code>.
	 *
	 * @param serializeLinksAsObjects flag which decides whether the {@link LinksInformationSerializer} should be added as
	 * additional serializer or not.
	 *
	 * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
	 */
	public static SimpleModule createJacksonModule(boolean serializeLinksAsObjects) {
		SimpleModule simpleModule = new SimpleModule(JSON_API_JACKSON_MODULE_NAME,
				new Version(1, 0, 0, null, null, null));
		simpleModule.addSerializer(new ErrorDataSerializer());
		simpleModule.addDeserializer(ErrorData.class, new ErrorDataDeserializer());
		if (serializeLinksAsObjects) {
			simpleModule.addSerializer(new LinksInformationSerializer());
		}
		return simpleModule;
	}
}
