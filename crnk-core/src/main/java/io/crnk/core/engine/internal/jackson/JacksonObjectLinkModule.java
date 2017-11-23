package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.module.Module;

public class JacksonObjectLinkModule implements Module {

	private static final String JACKSON_OBJECT_LINK_MODULE_NAME = "crnkLinkObject";

	private final ObjectMapper objectMapper;

	public JacksonObjectLinkModule(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public String getModuleName() {
		return "jacksonObjectLink";
	}

	@Override
	public void setupModule(ModuleContext context) {
		objectMapper.registerModule(createJacksonObjectLinkModule());

		DefaultResourceFieldInformationProvider defaultFieldProvider = new DefaultResourceFieldInformationProvider();
		ResourceFieldInformationProvider jacksonFieldProvider = new JacksonResourceFieldInformationProvider();

		// TODO move somewhere else and make use of a SerializerExtension
		context.addResourceInformationBuilder(new DefaultResourceInformationProvider(
				context.getPropertiesProvider(),
				defaultFieldProvider,
				jacksonFieldProvider));
	}

	/**
	 * Creates Crnk Jackson module with an additional serializer to serialize {@link io.crnk.core.resource.links.LinksInformation}
	 * as objects in JSON.
	 *
	 * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
	 */
	public static SimpleModule createJacksonObjectLinkModule() {
		SimpleModule simpleModule = new SimpleModule(JACKSON_OBJECT_LINK_MODULE_NAME,
				new Version(1, 0, 0, null, null, null));

		simpleModule.addSerializer(new ObjectLinkErrorDataSerializer());
		simpleModule.addDeserializer(ErrorData.class, new ObjectLinkErrorDataDeserializer());

		simpleModule.addSerializer(new LinksInformationSerializer());

		return simpleModule;
	}
}
