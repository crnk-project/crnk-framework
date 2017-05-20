package io.crnk.jpa.query;

import io.crnk.jpa.model.JoinedTableBaseEntity;
import io.crnk.jpa.model.JoinedTableChildEntity;
import org.junit.Test;

public abstract class JoinedTableInheritanceQueryTestBase
		extends AbstractInheritanceTest<JoinedTableBaseEntity, JoinedTableChildEntity> {

	public JoinedTableInheritanceQueryTestBase() {
		super(JoinedTableBaseEntity.class, JoinedTableChildEntity.class);
	}

	@Override
	@Test
	public void testOrderBySubtypeAttribute() {
		// NOTE those not work with JPA/Hibernate
		// so we do nothing here
	}
}
