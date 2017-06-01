package io.crnk.core.engine.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.internal.jackson.JsonApiModuleBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ErrorDataTest {

	@Test
	public void shouldFulfillEqualsHashCodeContract() throws Exception {
		EqualsVerifier.forClass(ErrorData.class).allFieldsShouldBeUsed().verify();
	}

	@Test
	public void testSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JsonApiModuleBuilder().build());

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
		Assert.assertEquals("ErrorData{id='null', aboutLink='null', status='status', code='code', title='title', detail='detail', sourcePointer='sourcePointer', sourceParameter='sourceParameter', meta=null}", actual);
	}

}