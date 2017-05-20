package io.crnk.core.engine.error;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ErrorResponseTest {

	@Test
	public void shouldFulfillHashcodeEqualsContract() throws Exception {
		EqualsVerifier.forClass(ErrorResponse.class).verify();
	}
}