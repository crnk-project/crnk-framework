package io.crnk.jpa.query;

import io.crnk.jpa.model.SingleTableBaseEntity;
import io.crnk.jpa.model.SingleTableChildEntity;

public abstract class SingleTableInheritanceQueryTestBase
		extends AbstractInheritanceTest<SingleTableBaseEntity, SingleTableChildEntity> {

	public SingleTableInheritanceQueryTestBase() {
		super(SingleTableBaseEntity.class, SingleTableChildEntity.class);
	}

}
