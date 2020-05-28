package io.crnk.gen.typescript.model;

import io.crnk.gen.typescript.model.writer.TSCodeStyle;
import io.crnk.gen.typescript.model.writer.TSWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class TSWriterTest {

	private TSWriter writer;

	private TSCodeStyle codeStyle;

	@Before
	public void setup() {
		codeStyle = new TSCodeStyle();
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
	public void writeEnum() {
		TSEnumType enumType = new TSEnumType();
		enumType.setName("TestEnum");
		enumType.getLiterals().add(new TSEnumLiteral("TEST_LITERAL_1"));
		enumType.getLiterals().add(new TSEnumLiteral("TEST_LITERAL_2"));

		enumType.accept(writer);
		Assert.assertEquals("\nenum TestEnum {\n" +
				"\tTEST_LITERAL_1 = 'TEST_LITERAL_1',\n" +
				"\tTEST_LITERAL_2 = 'TEST_LITERAL_2',\n" +
				"}", writer.toString());
	}

	@Test
	public void writeEnumLegacy() {
		codeStyle.setStringEnums(false);
		TSEnumType enumType = new TSEnumType();
		enumType.setName("TestEnum");
		enumType.getLiterals().add(new TSEnumLiteral("TEST_LITERAL_1"));
		enumType.getLiterals().add(new TSEnumLiteral("TEST_LITERAL_2"));

		enumType.accept(writer);
		Assert.assertEquals("\ntype TestEnum = 'TEST_LITERAL_1' | 'TEST_LITERAL_2';", writer.toString());
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
		classType.addImplementedInterface(interfaceType);

		classType.accept(writer);
		Assert.assertEquals("\nclass SomeClass implements SomeInterface {\n}", writer.toString());
	}

	@Test
	public void writeMemberNotSupported() {
		// must visit subtypes directly
		TSMember member = Mockito.mock(TSMember.class);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> writer.visit(member));
	}

	@Test
	public void writeClassWithIndex() {
		TSIndexSignatureType indexSignature = new TSIndexSignatureType();
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

	@Test
	public void writeClassWithEnumIndex() {
		TSIndexSignatureType indexSignature = new TSIndexSignatureType();
		TSEnumType enumKeyType = new TSEnumType();
		enumKeyType.setName("SampleEnum");
		indexSignature.setKeyType(enumKeyType);
		indexSignature.setValueType(TSPrimitiveType.NUMBER);

		TSClassType classType = new TSClassType();
		classType.setName("Child");
		classType.setIndexSignature(indexSignature);

		classType.accept(writer);
		Assert.assertEquals("\nclass Child {\n"
				+ "\t[key in SampleEnum]: number;\n"
				+ "}", writer.toString());
	}

	@Test
	public void writeClassWithInvalidAnyIndex() {
		TSIndexSignatureType indexSignature = new TSIndexSignatureType();
		indexSignature.setKeyType(TSAny.INSTANCE);
		indexSignature.setValueType(TSPrimitiveType.NUMBER);

		TSClassType classType = new TSClassType();
		classType.setName("Child");
		classType.setIndexSignature(indexSignature);

		Assertions.assertThrows(UnsupportedOperationException.class, () -> classType.accept(writer));
	}

	@Test
	public void writeParameterizedType() {
		TSParameterizedType type = new TSParameterizedType(null);
		Assertions.assertThrows(UnsupportedOperationException.class, () -> type.accept(writer));
	}

	@Test
	public void writeExport() {
		TSExport export = new TSExport();

		export.setAny(false);
		export.setPath("@test");
		export.addTypeName("a");
		export.addTypeName("b");

		export.accept(writer);
		Assert.assertEquals("export {a, b} from '@test';", writer.toString().trim());
	}

	@Test
	public void writeAnyExport() {
		TSExport export = new TSExport();

		export.setAny(true);
		export.setPath("@test");

		export.accept(writer);
		Assert.assertEquals("export * from '@test';", writer.toString().trim());
	}

}
