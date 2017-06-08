package io.crnk.gen.typescript;

import io.crnk.gen.typescript.internal.TypescriptUtils;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.test.mock.ClassTestUtils;
import org.gradle.internal.impldep.org.testng.Assert;
import org.junit.Test;

public class TypescriptUtilsTest {

	@Test
	public void checkHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(TypescriptUtils.class);
	}

	@Test
	public void getNestedInterfaesCreatesNewInterface() {
		TSModule module = new TSModule();
		module.setName("TestModule");

		TSClassType classType = new TSClassType();
		classType.setParent(module);
		classType.setName("TestClass");
		module.getElements().add(classType);

		TSInterfaceType testInterface = TypescriptUtils.getNestedInterface(classType, "TestInterface", true);
		Assert.assertEquals("TestInterface", testInterface.getName());
		Assert.assertTrue(testInterface.getParent() instanceof TSModule);
		Assert.assertEquals(module, testInterface.getParent().getParent());
		Assert.assertEquals("TestClass", ((TSModule) testInterface.getParent()).getName());

		Assert.assertEquals(2, module.getElements().size());
	}

	@Test
	public void getNestedInterfacesReturnsNullIfDoesNotExistsAndNonCreateRequested() {
		TSModule module = new TSModule();
		module.setName("TestModule");

		TSClassType classType = new TSClassType();
		classType.setParent(module);
		classType.setName("TestClass");
		module.getElements().add(classType);

		TSInterfaceType testInterface = TypescriptUtils.getNestedInterface(classType, "TestInterface", false);
		Assert.assertNull(testInterface);
	}

	@Test
	public void getNestedInterfacesReturnsNullIfNoParent() {
		TSClassType classType = new TSClassType();
		classType.setName("TestClass");

		TSInterfaceType testInterface = TypescriptUtils.getNestedInterface(classType, "TestInterface", false);
		Assert.assertNull(testInterface);
	}

	@Test(expected = IllegalStateException.class)
	public void getNestedInterfacesThrowsExceptionOnCreateIfNoParent() {
		TSClassType classType = new TSClassType();
		classType.setName("TestClass");

		TypescriptUtils.getNestedInterface(classType, "TestInterface", true);
	}
}
