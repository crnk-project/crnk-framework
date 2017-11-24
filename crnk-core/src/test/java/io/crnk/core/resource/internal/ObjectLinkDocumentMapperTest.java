package io.crnk.core.resource.internal;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.internal.jackson.JacksonObjectLinkModule;
import io.crnk.core.engine.properties.PropertiesProvider;

/**
 * @author AdNovum Informatik AG
 */
public class ObjectLinkDocumentMapperTest extends DocumentMapperTest {

	@Override
	public void setup() {
		super.setup();
		objectMapper.registerModule(JacksonObjectLinkModule.createJacksonObjectLinkModule());
	}

	@Override
	protected PropertiesProvider getPropertiesProvider() {
		return new PropertiesProvider() {
			@Override
			public String getProperty(String key) {
				if (key.equals(CrnkProperties.SERIALIZE_LINKS_AS_OBJECTS))
					return "true";
				return null;
			}
		};
	}

	@Override
	protected String getLinkText(JsonNode link) {
		return link.get("href").asText();
	}
}
