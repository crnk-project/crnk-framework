package io.crnk.core.repository.forward;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import io.crnk.core.repository.RelationshipRepositoryBase;
import org.junit.Test;

@Deprecated
public class RelationshipRepositoryBaseTest {

	@Test
	public void hasDefaultConstructor() {
		CoreClassTestUtils.assertProtectedConstructor(RelationshipRepositoryBase.class);
	}
}
