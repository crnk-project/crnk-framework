package io.crnk.core.engine.internal.dispatcher.path;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class PathIdsTest {

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(PathIds.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
}
