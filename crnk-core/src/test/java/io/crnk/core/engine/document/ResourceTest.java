package io.crnk.core.engine.document;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class ResourceTest {

	@Test
	public void testResourceEqualsContract() {
		EqualsVerifier.forClass(Resource.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
