package io.crnk.core.queryspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PathSpecTest {

	@Test
	public void testSerialization() throws IOException {
		PathSpec pathSpec = PathSpec.of("a.b.c");
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writerFor(PathSpec.class).writeValueAsString(pathSpec);
		Assert.assertEquals("\"a.b.c\"", json);

		PathSpec clone = objectMapper.readerFor(PathSpec.class).readValue(json);
		Assert.assertEquals(pathSpec, clone);
	}
}
