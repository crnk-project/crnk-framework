package io.crnk.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.client.response.JsonLinksInformation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingBehavior;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

public class PageNumberSizeClientTest extends AbstractClientTest {

	@Before
	public void setup() {
		super.setup();

		DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) client.getUrlMapper();
		urlMapper.setAllowUnknownAttributes(true);
	}


	@Override
	protected TestApplication configure() {
		TestApplication app = new TestApplication();
		app.getFeature().addModule(NumberSizePagingBehavior.createModule());
		return app;
	}

	@Override
	protected void setupClient(CrnkClient client) {
		client.addModule(NumberSizePagingBehavior.createModule());
	}


	@Test
	public void testDefault() {
		ResourceRepository<Schedule, Serializable> repo = client.getRepositoryForType(Schedule.class);
		for (int i = 100; i < 120; i++) {
			Schedule schedule = new Schedule();
			schedule.setName("someSchedule" + i);
			repo.create(schedule);
		}

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.setPaging(new NumberSizePagingSpec(2, 5));
		ResourceList<Schedule> list = repo.findAll(querySpec);
		Assert.assertEquals(5, list.size());
		Schedule schedule = list.get(0);
		Assert.assertEquals("someSchedule105", schedule.getName());

		String url = client.getServiceUrlProvider().getUrl();
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		JsonNode firstLink = links.asJsonNode().get("first");
		Assert.assertNotNull(firstLink);
		Assert.assertEquals(url + "/schedules?page[number]=1&page[size]=5", firstLink.asText());
	}

	@Test
	public void testOffsetLimitInteroperablity() {
		JsonApiResource annotation = Task.class.getAnnotation(JsonApiResource.class);
		Assert.assertEquals(OffsetLimitPagingSpec.class, annotation.pagingSpec());
		ResourceRepository<Task, Serializable> repo = client.getRepositoryForType(Task.class);
		for (int i = 100; i < 120; i++) {
			Task task = new Task();
			task.setName("someTask" + i);
			repo.create(task);
		}

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.setPaging(new NumberSizePagingSpec(2, 5));
		ResourceList<Task> list = repo.findAll(querySpec);
		Assert.assertEquals(5, list.size());
		Task task = list.get(0);
		Assert.assertEquals("someTask105", task.getName());

		String url = client.getServiceUrlProvider().getUrl();
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		JsonNode firstLink = links.asJsonNode().get("first");
		Assert.assertNotNull(firstLink);
		Assert.assertEquals(url + "/tasks?page[number]=1&page[size]=5", firstLink.asText());
	}
}