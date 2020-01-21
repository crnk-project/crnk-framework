package io.crnk.client.response;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.JsonLinksInformation;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
		Assert.assertEquals("test", meta.value.getHref());
	}

	@Test
	public void testInterfaceProxy() {
		JsonLinksInformation info = new JsonLinksInformation(node, mapper);

		LinksInterface meta = info.as(LinksInterface.class);
		Assert.assertEquals("test", meta.getValue());
	}

	interface LinksInterface extends LinksInformation {

		String getValue();
	}


	@Test(expected = IllegalStateException.class)
	public void testParseException() {
		JsonLinksInformation info = new JsonLinksInformation(node, mapper);

		info.as(ScheduleRepository.ScheduleListLinks.class);
	}


}
