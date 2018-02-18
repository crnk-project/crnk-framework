package io.crnk.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.client.action.JerseyActionStubFactory;
import io.crnk.client.module.ClientTestModule;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.CrnkFeature;
import io.crnk.rs.JsonApiResponseFilter;
import io.crnk.rs.JsonapiExceptionMapperBridge;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.ProjectToTaskRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.mock.repository.TaskRepository;
import io.crnk.test.mock.repository.TaskToProjectRepository;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Before;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.MultivaluedMap;

public abstract class AbstractClientTest extends JerseyTestBase {

	protected CrnkClient client;

	protected TestApplication testApplication;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Before
	public void setup() {
		createClient();
		setupClient(client);

		cleanRepositories();

		Assert.assertNotNull(client.getActionStubFactory());
		Assert.assertNotNull(client.getModuleRegistry());
	}

	protected void createClient() {
		client = new CrnkClient(getBaseUri().toString());
		client.addModule(new ClientTestModule());
		// tag::jerseyStubFactory[]
		client.setActionStubFactory(JerseyActionStubFactory.newInstance());
		// end::jerseyStubFactory[]
		client.getHttpAdapter().setReceiveTimeout(10000000, TimeUnit.MILLISECONDS);
	}

	protected void setupClient(CrnkClient client) {

	}

	protected void cleanRepositories() {
		TaskRepository.clear();
		ProjectRepository.clear();
		TaskToProjectRepository.clear();
		ProjectToTaskRepository.clear();
		ScheduleRepositoryImpl.clear();
	}

	@Override
	protected TestApplication configure() {
		if (testApplication == null) {
			testApplication = new TestApplication(false);
		}

		return testApplication;
	}

	protected void setupFeature(CrnkTestFeature feature) {
		// nothing to do
	}

	/**
	 * Assert the specified header name has the specified value.
	 */
	protected void assertHasHeaderValue(String name, String value) {
		MultivaluedMap<String, String> headers = getLastReceivedHeaders();
		Assert.assertNotNull(headers);

		List<String> values = headers.get(name);
		Assert.assertNotNull(values);

		Assert.assertTrue(values.contains(value));
	}

	/**
	 * Clear the last received headers.
	 */
	protected void clearLastReceivedHeaders() {
		getTestFilter().clearLastReceivedHeaders();
	}

	/**
	 * Return the last received headers.
	 */
	private MultivaluedMap<String, String> getLastReceivedHeaders() {
		return getTestFilter().getLastReceivedHeaders();
	}

	/**
	 * Return the configured test filter.
	 */
	private TestRequestFilter getTestFilter() {
		return ((CrnkTestFeature) testApplication.getFeature()).getTestFilter();
	}

	@ApplicationPath("/")
	public class TestApplication extends ResourceConfig {

		private CrnkTestFeature feature;

		public TestApplication(boolean querySpec) {
			this(querySpec, false, false);
		}

		public TestApplication(boolean querySpec, boolean jsonApiFilter, boolean serializeLinksAsObjects) {
			property(CrnkProperties.SERIALIZE_LINKS_AS_OBJECTS, Boolean.toString(serializeLinksAsObjects));
			if (!querySpec) {
				feature = new CrnkTestFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()),
						new SampleJsonServiceLocator());
			}
			else {
				feature = new CrnkTestFeature(new ObjectMapper(), new DefaultQuerySpecDeserializer(),
						new SampleJsonServiceLocator());
			}

			feature.addModule(new io.crnk.test.mock.TestModule());
			feature.addModule(new ClientTestModule());

			if (jsonApiFilter) {
				register(new JsonApiResponseFilter(feature));
				register(new JsonapiExceptionMapperBridge(feature));
				register(new JacksonFeature());
			}

			setupFeature(feature);

			register(feature);
		}

		public CrnkFeature getFeature() {
			return feature;
		}
	}
}
