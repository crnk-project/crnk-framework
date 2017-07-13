package io.crnk.rs;

import javax.ws.rs.core.Application;

/**
 * @author AdNovum Informatik AG
 */
public class JsonApiResponseFilterWrapTest  extends JsonApiResponseFilterTestBase {

	@Override
	protected Application configure() {
		return new JsonApiResponseFilterTestBase.TestApplication(this, false, true);
	}

}
