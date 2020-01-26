package io.crnk.client;

import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class ParameterPropagationClientTest extends AbstractClientTest {

	@Override
	protected void setupFeature(CrnkTestFeature feature) {
		ModuleRegistry moduleRegistry = feature.getBoot().getModuleRegistry();
		moduleRegistry.getUrlBuilder().addPropagatedParameter("test");
	}

	@Test
	public void verifyParameterPropagated() throws IOException {
		ResourceRepository<Schedule, Object> repository = client.getRepositoryForType(Schedule.class);
		Schedule schedule = new Schedule();
		schedule.setId(12L);
		schedule.setName("someTask");
		repository.create(schedule);

		String url = client.getServiceUrlProvider().getUrl() + "/schedules?test=propagatedValue";
		HttpAdapterRequest request = client.getHttpAdapter().newRequest(url, HttpMethod.GET, null);
		HttpAdapterResponse response = request.execute();
		Assert.assertEquals(200, response.code());

		String body = response.body();
		Assert.assertTrue(body.contains("/schedules/12?test=propagatedValue"));
		Assert.assertTrue(body.contains("/schedules/12/relationships/taskSet?test=propagatedValue"));
		Assert.assertTrue(body.contains("/schedules/12/taskSet?test=propagatedValue"));
	}
}