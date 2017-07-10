package io.crnk.rs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import io.crnk.rs.type.JsonApiMediaType;
import org.junit.Assert;
import org.junit.Test;

public class JsonApiResponseFilterTest extends JsonApiResponseFilterTestBase {

	@Override
	protected Application configure() {
		return new JsonApiResponseFilterTestBase.TestApplication(false);
	}

	@Test
	public void testNullResponse() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API disabled

		// WHEN
		Response response = get("/repositoryActionWithNullResponse", null, true);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
	}

	@Test
	public void testWrappedJsonApiResponse() throws Exception {
		// GIVEN
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("msg", "msg");

		// WHEN
		Response response = get("/repositoryAction", queryParams, true);

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
				.startsWith("{\"data\":\"repository action: msg\"}");
	}

	@Test
	public void testStringResponse() throws Exception {
		// GIVEN
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("msg", "msg");

		// WHEN
		Response response = get("/repositoryAction", queryParams, false);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		String entity = response.readEntity(String.class);
		assertThat(entity)
				.describedAs("Response content")
				.isEqualTo("repository action: msg");
	}

	@Test
	public void testPlainResourceResponse() throws Exception {
		// GIVEN

		// WHEN
		Response response = get("", null, false);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
			.describedAs("Status code")
			.isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

		String error = response.readEntity(String.class);
		assertThat(error)
			.describedAs("Response content")
			.startsWith("{\"errors\":");
	}

	@Test
	public void testJsonApiResourceResponse() throws Exception {
		// GIVEN

		// WHEN
		Response response = get("", null, true);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getStatus())
				.describedAs("Status code")
				.isEqualTo(Response.Status.OK.getStatusCode());

		String schedules = response.readEntity(String.class);
		assertThat(schedules)
				.describedAs("Response content")
				.startsWith("{\"data\":").contains("\"links\":{").contains("\"meta\":{");
	}

	// GIVEN

	// WHEN

	// THEN

}
