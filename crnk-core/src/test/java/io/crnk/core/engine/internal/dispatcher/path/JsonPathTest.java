package io.crnk.core.engine.internal.dispatcher.path;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.mockito.Mockito;

public class JsonPathTest {

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(JsonPath.class)
				.usingGetClass()
				.withPrefabValues(JsonPath.class, Mockito.mock(JsonPath.class), Mockito.mock(JsonPath.class))
				.suppress(Warning.NONFINAL_FIELDS)
				.suppress(Warning.REFERENCE_EQUALITY)
				.verify();
	}
}
