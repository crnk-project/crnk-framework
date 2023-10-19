package io.crnk.rs;

import jakarta.ws.rs.core.Application;

/**
 * @author AdNovum Informatik AG
 */
public class JsonApiResponseFilterTest extends JsonApiResponseFilterTestBase {

	@Override
	protected Application configure() {
		return new JsonApiResponseFilterTestBase.TestApplication(this, false);
	}

}
