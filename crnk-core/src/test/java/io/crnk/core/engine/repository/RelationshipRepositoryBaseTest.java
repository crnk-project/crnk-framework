package io.crnk.core.engine.repository;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import io.crnk.core.repository.RelationshipRepositoryBase;
import org.junit.Test;

public class RelationshipRepositoryBaseTest {

	@Test
	public void hasDefaultConstructor() {
		CoreClassTestUtils.assertProtectedConstructor(RelationshipRepositoryBase.class);
	}
}
