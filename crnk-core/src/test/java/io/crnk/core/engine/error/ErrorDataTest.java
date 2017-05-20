package io.crnk.core.engine.error;

import io.crnk.core.engine.document.ErrorData;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ErrorDataTest {

	@Test
	public void shouldFulfillEqualsHashCodeContract() throws Exception {
		EqualsVerifier.forClass(ErrorData.class).allFieldsShouldBeUsed().verify();
	}

}