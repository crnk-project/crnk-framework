package io.crnk.client.response;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.meta.JsonMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

	@Test
	public void testInterfaceProxy() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);

		MetaInterface meta = info.as(MetaInterface.class);
		Assert.assertEquals("test", meta.getValue());
	}

	interface MetaInterface extends MetaInformation {

		String getValue();
	}


	@Test(expected = IllegalStateException.class)
	public void testParseException() {
		JsonMetaInformation info = new JsonMetaInformation(node, mapper);

		info.as(ScheduleRepository.ScheduleListMeta.class);
	}


}
