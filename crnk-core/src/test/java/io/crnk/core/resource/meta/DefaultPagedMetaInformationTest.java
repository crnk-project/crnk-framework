package io.crnk.core.resource.meta;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Test;

public class DefaultPagedMetaInformationTest {

	@Test
	public void nullMustNotBeSerialized() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerFor(DefaultPagedMetaInformation.class);

		DefaultPagedMetaInformation metaInformation = new DefaultPagedMetaInformation();
		String json = writer.writeValueAsString(metaInformation);
		Assert.assertEquals("{}", json);
	}

	@Test
	public void nonNullMustBeSerialized() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writerFor(DefaultPagedMetaInformation.class);

		DefaultPagedMetaInformation metaInformation = new DefaultPagedMetaInformation();
		metaInformation.setTotalResourceCount(12L);

		String json = writer.writeValueAsString(metaInformation);
		Assert.assertEquals("{\"totalResourceCount\":12}", json);
	}
}
