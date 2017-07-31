package io.crnk.gen.typescript.model;

import io.crnk.gen.typescript.writer.TSCodeStyle;
import io.crnk.gen.typescript.writer.TSWriter;
import org.gradle.internal.impldep.org.testng.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TSWriterTest {


	private TSWriter writer;

	@Before
	public void setup() {
		TSCodeStyle codeStyle = new TSCodeStyle();
		writer = new TSWriter(codeStyle);
	}

	@Test
	public void writeArray() {
		TSArrayType arrayType = new TSArrayType(TSPrimitiveType.STRING);
		arrayType.accept(writer);
		Assert.assertEquals("Array<string>", writer.toString());
	}

	@Test
	public void writeClassWithSuperType() {
		TSClassType classSuperType = new TSClassType();
		classSuperType.setName("Base");

		TSClassType classType = new TSClassType();
		classType.setName("Child");
		classType.setSuperType(classSuperType);

		classType.accept(writer);
		Assert.assertEquals("\nclass Child extends Base {\n}", writer.toString());
	}

	@Test
	public void writePrimitiveType() {
		TSPrimitiveType.STRING.accept(writer);
		Assert.assertEquals("string", writer.toString());
	}

	@Test
	public void writeAnyType() {
		TSAny.INSTANCE.accept(writer);
		Assert.assertEquals("any", writer.toString());
	}

	@Test
	public void writeClassWithImplements() {
		TSInterfaceType interfaceType = new TSInterfaceType();
		interfaceType.setName("SomeInterface");

		TSClassType classType = new TSClassType();
		classType.setName("SomeClass");
		classType.getImplementedInterfaces().add(interfaceType);

		classType.accept(writer);
		Assert.assertEquals("\nclass SomeClass implements SomeInterface {\n}", writer.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void writeMemberNotSupported() {
		// must visit subtypes directly
		TSMember member = Mockito.mock(TSMember.class);
		writer.visit(member);
	}

	@Test
	public void writeClassWithIndex() {
		TSIndexSignature indexSignature = new TSIndexSignature();
		indexSignature.setKeyType(TSPrimitiveType.STRING);
		indexSignature.setValueType(TSPrimitiveType.NUMBER);

		TSClassType classType = new TSClassType();
		classType.setName("Child");
		classType.setIndexSignature(indexSignature);

		classType.accept(writer);
		Assert.assertEquals("\nclass Child {\n"
				+ "\t[key: string]: number;\n"
				+ "}", writer.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void writeParameterizedType() {
		TSParameterizedType type = new TSParameterizedType(null);
		type.accept(writer);
	}

	@Test
	public void writeExport() {
		TSExport export = new TSExport();

		export.setAny(false);
		export.setPath("@test");
		export.addTypeName("a");
		export.addTypeName("b");

		export.accept(writer);
		Assert.assertEquals("export {a, b} from '@test'", writer.toString().trim());
	}

	@Test
	public void writeAnyExport() {
		TSExport export = new TSExport();

		export.setAny(true);
		export.setPath("@test");

		export.accept(writer);
		Assert.assertEquals("export * from '@test'", writer.toString().trim());
	}

}
