package io.crnk.client.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class JsonLinksInformationTest {

	private ObjectMapper mapper;
	private JsonNode node;

	@Before
	public void setup() throws IOException {
		mapper = new ObjectMapper();
		node = mapper.reader().readTree("{\"value\": \"test\"}");
	}

	@Test
	public void testAsNode() {
		JsonLinksInformation info = new JsonLinksInformation(node, mapper);
		Assert.assertSame(node, info.asJsonNode());
	}

	@Test
	public void testParse() {
		JsonLinksInformation info = new JsonLinksInformation(node, mapper);

		Task.TaskLinks meta = info.as(Task.TaskLinks.class);
		Assert.assertEquals("test", meta.value);
	}

	@Test(expected = IllegalStateException.class)
	public void testParseException() {
		JsonLinksInformation info = new JsonLinksInformation(node, mapper);

		info.as(ScheduleRepository.ScheduleListLinks.class);
	}


}
