package io.crnk.data.jpa.query;

import io.crnk.data.jpa.model.TablePerClassBaseEntity;
import io.crnk.data.jpa.model.TablePerClassChildEntity;

public abstract class TablePerClassInhertitanceQueryTestBase extends AbstractInheritanceTest<TablePerClassBaseEntity, TablePerClassChildEntity> {

	public TablePerClassInhertitanceQueryTestBase() {
		super(TablePerClassBaseEntity.class, TablePerClassChildEntity.class);
	}

}
