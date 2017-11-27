package io.crnk.core.engine.error;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperUtil;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.properties.PropertiesProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author AdNovum Informatik AG
 */
public class ObjectLinkErrorDataTest extends ErrorDataTest {

	@Before
	public void setup() {
		util = new DocumentMapperUtil(null, null, new PropertiesProvider() {
			@Override
			public String getProperty(String key) {
				if (key.equals(CrnkProperties.SERIALIZE_LINKS_AS_OBJECTS))
					return "true";
				return null;
			}
		});
	}

	@Test
	@Override
	public void testSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule(true));

		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setAboutLink("about");
		builder.setCode("code");
		builder.setDetail("detail");
		builder.setId("id");
		builder.setSourcePointer("sourcePointer");
		builder.setSourceParameter("sourceParameter");
		builder.setStatus("status");
		builder.setTitle("title");
		builder.addMetaField("meta1", "value1");

		ErrorData errorData = builder.build();
		String json = mapper.writeValueAsString(errorData);
		Assert.assertTrue(json.contains("{\"about\":{\"href\":\"about\"}}"));
		ErrorData copy = mapper.readerFor(ErrorData.class).readValue(json);

		Assert.assertEquals(errorData, copy);
	}
}
