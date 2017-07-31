package io.crnk.gen.typescript.model;

import org.junit.Test;
import org.mockito.Mockito;

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

	@Test
	public void visitInterfaceShouldVisitMembers() {
		TSMember member = Mockito.mock(TSMember.class);
		TSInterfaceType interfaceType = new TSInterfaceType();
		interfaceType.addDeclaredMember(member);

		TSVisitorBase base = new TSVisitorBase();
		base.visit(interfaceType);
		Mockito.verify(member, Mockito.times(1)).accept(Mockito.eq(base));
	}


	@Test
	public void visitClassShouldVisitMembers() {
		TSMember member = Mockito.mock(TSMember.class);
		TSClassType classType = new TSClassType();
		classType.addDeclaredMember(member);

		TSVisitorBase base = new TSVisitorBase();
		base.visit(classType);
		Mockito.verify(member, Mockito.times(1)).accept(Mockito.eq(base));
	}
}
