package io.crnk.rs;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import org.junit.BeforeClass;


public abstract class JsonApiResponseFilterTestBase extends JerseyTestBase {

	private static final String BASE_PATH = "schedules";

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new JettyTestContainerFactory();
	}

	private static Client httpClient;

	@BeforeClass
	public static void setup() {
		ClientConfig config = new ClientConfig();
		config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		httpClient = ClientBuilder.newClient(config);
	}

	@ApplicationPath("/")
	static class TestApplication extends ResourceConfig {

		TestApplication(boolean enableNullResponse) {
			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.rs.resource");
			if (enableNullResponse) {
				property(CrnkProperties.NULL_DATA_RESPONSE_ENABLED, "true");
			}

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(new TestModule());

			register(new JsonApiResponseFilter(feature));
			register(new JsonapiExceptionMapperBridge(feature));
			register(new JacksonFeature());

			register(feature);
		}

	}

	Response get(String path, Map<String, String> queryParams, boolean requestJsonApi) {
		return request(path, queryParams, requestJsonApi).get();
	}

	private Invocation.Builder request(String path, Map<String, String> queryParams, boolean requestJsonApi) {
		WebTarget target = httpClient.target(getBaseUri() + BASE_PATH).path(path);
		if (queryParams != null && !queryParams.isEmpty()) {
			Set<String> keys = queryParams.keySet();
			for (String key : keys) {
				target = target.queryParam(key, queryParams.get(key));
			}
		}
		return target.request().accept(requestJsonApi ? JsonApiMediaType.APPLICATION_JSON_API : MediaType.TEXT_PLAIN);
	}

}
