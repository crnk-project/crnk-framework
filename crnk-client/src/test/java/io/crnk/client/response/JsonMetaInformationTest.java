package io.crnk.client.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class JsonMetaInformationTest {

	private ObjectMapper mapper;
	private JsonNode node;

	@Before
	public void setup() throws IOException {
		mapper = new ObjectMapper();
		node = mapper.reader().readTree("{\"value\": \"test\"}");
	}

	@Test
	public void testAsNode() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);
		Assert.assertSame(node, info.asJsonNode());
	}

	@Test
	public void testParse() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);

		Task.TaskMeta meta = info.as(Task.TaskMeta.class);
		Assert.assertEquals("test", meta.value);
	}

	@Test(expected = IllegalStateException.class)
	public void testParseException() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);

		info.as(ScheduleRepository.ScheduleListMeta.class);
	}


}
