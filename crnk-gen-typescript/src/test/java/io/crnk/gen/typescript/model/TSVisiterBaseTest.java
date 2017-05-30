package io.crnk.gen.typescript.model;

import org.junit.Test;

public class TSVisiterBaseTest {

	@Test
	public void checkDoesNothing() {
		// get sonar coverage
		TSVisitorBase base = new TSVisitorBase();

		base.visit((TSArrayType) null);
		base.visit((TSEnumType) null);
		base.visit((TSPrimitiveType) null);
		base.visit((TSMember) null);
		base.visit((TSIndexSignature) null);
		base.visit((TSField) null);
		base.visit((TSAny) null);
		base.visit((TSImport) null);
		base.visit((TSExport) null);
		base.visit((TSParameterizedType) null);
	}
}
