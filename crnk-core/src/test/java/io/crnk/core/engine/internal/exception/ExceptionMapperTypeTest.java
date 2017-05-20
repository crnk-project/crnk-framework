package io.crnk.core.engine.internal.exception;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ExceptionMapperTypeTest {

	@Test
	public void shouldFulfillHashCodeEqualsContract() throws Exception {
		EqualsVerifier.forClass(ExceptionMapperType.class).verify();
	}
}