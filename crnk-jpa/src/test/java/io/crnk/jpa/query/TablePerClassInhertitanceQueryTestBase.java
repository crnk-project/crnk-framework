package io.crnk.jpa.query;

import io.crnk.jpa.model.TablePerClassBaseEntity;
import io.crnk.jpa.model.TablePerClassChildEntity;

public abstract class TablePerClassInhertitanceQueryTestBase extends AbstractInheritanceTest<TablePerClassBaseEntity, TablePerClassChildEntity> {

	public TablePerClassInhertitanceQueryTestBase() {
		super(TablePerClassBaseEntity.class, TablePerClassChildEntity.class);
	}

}
