package io.crnk.client;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingBehavior;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Schedule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PageNumberSizeClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Schedule, Long> taskRepo;


	@Before
	public void setup() {
		super.setup();

		DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) client.getUrlMapper();
		urlMapper.setAllowUnknownAttributes(true);

		taskRepo = client.getRepositoryForType(Schedule.class);
	}


	@Override
	protected TestApplication configure() {
		TestApplication app = new TestApplication(true);
		app.getFeature().addModule(NumberSizePagingBehavior.createModule());
		return app;
	}

	@Override
	protected void setupClient(CrnkClient client) {
		client.addModule(NumberSizePagingBehavior.createModule());
	}


	@Test
	public void test() {
		for (int i = 100; i < 120; i++) {
			Schedule schedule = new Schedule();
			schedule.setName("someSchedule" + i);
			taskRepo.create(schedule);
		}

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.setPaging(new NumberSizePagingSpec(1, 5));
		ResourceList<Schedule> list = taskRepo.findAll(querySpec);
		Assert.assertEquals(5, list.size());
		Schedule schedule = list.get(0);
		Assert.assertEquals("someSchedule105", schedule.getName());

		String url = client.getServiceUrlProvider().getUrl();
		PagedLinksInformation links = list.getLinks(PagedLinksInformation.class);
		Assert.assertEquals(url + "/tasks?page[number]=0&page[size]=5", links.getFirst());
	}
}