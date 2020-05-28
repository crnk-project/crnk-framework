package io.crnk.core.engine.error;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperUtil;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ErrorDataTest {

	protected DocumentMapperUtil util;

	@Before
	public void setup() {
		util = new DocumentMapperUtil(null, null, new NullPropertiesProvider(), new JsonApiUrlBuilder(null));
	}

	@Test
	public void shouldFulfillEqualsHashCodeContract() {
		EqualsVerifier.forClass(ErrorData.class).allFieldsShouldBeUsed().verify();
	}

	@Test
	public void testSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule());

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

		Assert.assertTrue(json.contains("\"parameter\":\"sourceParameter\""));
		Assert.assertTrue(json.contains("\"pointer\":\"sourcePointer\""));
		Assert.assertTrue(json.contains("\"meta\":{\"meta1\":\"value1\"}"));
		Assert.assertTrue(json.contains("\"links\":{\"about\":\"about\"}"));

		ErrorData copy = mapper.readerFor(ErrorData.class).readValue(json);

		Assert.assertEquals(errorData, copy);
	}

	@Test
	public void testToString() {
		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setTitle("title");
		builder.setCode("code");
		builder.setStatus("status");
		builder.setDetail("detail");
		builder.setSourcePointer("sourcePointer");
		builder.setSourceParameter("sourceParameter");
		String actual = builder.build().toString();
		Assert.assertEquals(
				"ErrorData{id='null', aboutLink='null', status='status', code='code', title='title', detail='detail', "
						+ "sourcePointer='sourcePointer', sourceParameter='sourceParameter', meta=null}",
				actual);
	}

}