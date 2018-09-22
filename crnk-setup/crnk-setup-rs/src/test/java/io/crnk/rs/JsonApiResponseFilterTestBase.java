package io.crnk.rs;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.rs.type.JsonApiMediaType;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestModule;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public abstract class JsonApiResponseFilterTestBase extends JerseyTestBase {

	private static final String BASE_PATH = "schedules";

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new JettyTestContainerFactory();
	}

	private static Client httpClient;
	private boolean enableNullResponse;

	@BeforeClass
	public static void setup() {
		ClientConfig config = new ClientConfig();
		config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		httpClient = ClientBuilder.newClient(config);
	}

	// tag::docs[]
	@ApplicationPath("/")
	class TestApplication extends ResourceConfig {

		TestApplication(JsonApiResponseFilterTestBase instance, boolean enableNullResponse) {
			instance.setEnableNullResponse(enableNullResponse);

			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.rs.resource");
			property(CrnkProperties.NULL_DATA_RESPONSE_ENABLED, Boolean.toString(enableNullResponse));

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(new TestModule());

			register(new JsonApiResponseFilter(feature));
			register(new JsonapiExceptionMapperBridge(feature));
			register(new JacksonFeature());

			register(feature);
		}
	}
	// end::docs[]

	void setEnableNullResponse(boolean enableNullResponse) {
		this.enableNullResponse = enableNullResponse;
	}

	Response get(String path, Map<String, String> queryParams) {
		return request(path, queryParams).get();
	}

	private Invocation.Builder request(String path, Map<String, String> queryParams) {
		WebTarget target = httpClient.target(getBaseUri() + BASE_PATH).path(path);
		if (queryParams != null && !queryParams.isEmpty()) {
			Set<String> keys = queryParams.keySet();
			for (String key : keys) {
				target = target.queryParam(key, queryParams.get(key));
			}
		}
		return target.request();
	}

	@Test
	public void testNullResponseNotWrapped() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API enabled, but method produces text/plain -> no wrapping
		Assume.assumeFalse(enableNullResponse);

		// WHEN
		Response response = get("/repositoryActionWithNullResponse", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
		assertThat(response.getMediaType())
				.describedAs("Media-Type")
				.isEqualTo(null);

		Object entity = response.readEntity(Object.class);
		assertThat(entity)
				.describedAs("Response content")
				.isEqualTo(null);
	}

	@Test
	public void testNullResponseJsonApi() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API enabled
		Assume.assumeTrue(enableNullResponse);

		// WHEN
		Response response = get("/repositoryActionWithNullResponseJsonApi", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());
		assertThat(response.getMediaType())
				.describedAs("Media-Type")
				.isEqualTo(JsonApiMediaType.APPLICATION_JSON_API_TYPE);

		String entity = response.readEntity(String.class);
		assertThat(entity)
				.describedAs("Response content")
				.isEqualTo("{\"data\":null}");
	}

	@Test
	public void testNullResponse() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API disabled
		Assume.assumeFalse(enableNullResponse);

		// WHEN
		Response response = get("/repositoryActionWithNullResponse", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(null);
	}

	@Test
	public void testNonInterfaceMethodWithNullResponseJsonApi() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API disabled
		Assume.assumeFalse(enableNullResponse);

		// WHEN
		Response response = get("/nonInterfaceMethodWithNullResponseJsonApi", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(null);
	}

	@Test
	public void testNonInterfaceMethodWithNullResponseJsonApiWrapped() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API enabled
		Assume.assumeTrue(enableNullResponse);

		// WHEN
		Response response = get("/nonInterfaceMethodWithNullResponseJsonApi", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(JsonApiMediaType.APPLICATION_JSON_API_TYPE);
		String schedule = response.readEntity(String.class);
		assertThat(schedule)
				.describedAs("Response content")
				.isEqualTo("{\"data\":null}");
	}

	@Test
	public void testStringResponse() throws Exception {
		// GIVEN
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("msg", "msg");

		// WHEN
		Response response = get("/repositoryAction", queryParams);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(MediaType.TEXT_HTML_TYPE);
		String entity = response.readEntity(String.class);
		assertThat(entity)
				.describedAs("Response content")
				.isEqualTo("repository action: msg");
	}

	@Test
	public void testStringResponseWrapped() throws Exception {
		// GIVEN
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("msg", "msg");

		// WHEN
		Response response = get("/repositoryActionJsonApi", queryParams);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(JsonApiMediaType.APPLICATION_JSON_API_TYPE);
		String entity = response.readEntity(String.class);
		assertThat(entity)
				.describedAs("Response content")
				.isEqualTo("{\"data\":\"repository action: msg\"}");
	}

	@Test
	public void testErrorResponse() throws Exception {
		// GIVEN

		// WHEN
		Response response = get("/repositoryActionWithException", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(JsonApiMediaType.APPLICATION_JSON_API_TYPE);
		String error = response.readEntity(String.class);
		assertThat(error)
				.describedAs("Response content")
				.startsWith("{\"errors\":");
	}

	@Test
	public void testJsonApiResourceListResponse() throws Exception {
		// GIVEN

		// WHEN
		Response response = get("", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		// media type contains charset with this response
		String mediaType = response.getMediaType().toString();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.startsWith(JsonApiMediaType.APPLICATION_JSON_API);
		String schedules = response.readEntity(String.class);
		assertThat(schedules)
				.describedAs("Response content")
				.contains("\"data\" :")
				.contains("\"links\" :")
				.contains("\"meta\" :");
	}

	@Test
	public void testJsonApiResourceResponse() throws Exception {
		// GIVEN

		// WHEN
		Response response = get("/repositoryActionWithResourceResult", null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		MediaType mediaType = response.getMediaType();
		assertThat(mediaType)
				.describedAs("Media-Type")
				.isEqualTo(JsonApiMediaType.APPLICATION_JSON_API_TYPE);
		String schedule = response.readEntity(String.class);
		assertThat(schedule)
				.describedAs("Response content")
				.startsWith("{\"data\":").contains("\"links\":{");
	}

}
