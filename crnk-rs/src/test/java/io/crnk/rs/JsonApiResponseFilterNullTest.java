package io.crnk.rs;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import io.crnk.rs.type.JsonApiMediaType;
import org.junit.Assert;
import org.junit.Test;

public class JsonApiResponseFilterNullTest extends JsonApiResponseFilterTestBase {

	@Override
	protected Application configure() {
		return new JsonApiResponseFilterTestBase.TestApplication(true);
	}

	@Test
	public void testNullResponse() throws Exception {
		// GIVEN
		// mapping of null responses to JSON-API enabled

		// WHEN
		Response response = get("/repositoryActionWithNullResponse", null, true);

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

}
