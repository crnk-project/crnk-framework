package io.crnk.rs;

import jakarta.ws.rs.core.Application;

public class JsonApiResponseFilterNullTest extends JsonApiResponseFilterTestBase {

	@Override
	protected Application configure() {
		return new JsonApiResponseFilterTestBase.TestApplication(this, true);
	}

}
