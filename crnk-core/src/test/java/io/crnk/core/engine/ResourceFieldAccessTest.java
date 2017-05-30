package io.crnk.core.engine;

import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ResourceFieldAccessTest {


	@Test
	public void testEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(ResourceFieldAccess.class).usingGetClass().verify();
	}
}
