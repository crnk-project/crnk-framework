package io.crnk.rs;

import javax.ws.rs.core.Application;

public class JsonApiResponseFilterAllOnTest extends JsonApiResponseFilterTestBase {

	@Override
	protected Application configure() {
		return new TestApplication(this, true, true);
	}

}
