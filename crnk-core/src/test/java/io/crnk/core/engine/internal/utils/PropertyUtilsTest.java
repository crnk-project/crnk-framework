package io.crnk.core.engine.internal.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class PropertyUtilsTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onNullBeanGetShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.getProperty(null, "privatePropertyWithMutators");
	}

	@Test
	public void onBooleanWithGetPrefix() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanWithGetPrefix(true);

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "booleanWithGetPrefix");

		// THEN
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onNullFieldGetShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.getProperty(bean, (String) null);
	}

	@Test
	public void onBooleanPrimitiveWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanPrimitivePropertyWithMutators(true);

		// WHEN
		Object result = PropertyUtils
				.getProperty(bean, "booleanPrimitivePropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo(true);
	}


	@Test
	public void methodPropertyShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		Object result = PropertyUtils
				.getProperty(bean, "methodProperty");

		// THEN
		assertThat(result).isEqualTo("noFieldsHere");
	}

	@Test
	public void getPropertyClassForMethodPropertyShouldReturnClass() throws Exception {
		// WHEN
		Object result = PropertyUtils.getPropertyClass(Bean.class, "methodProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void getPropertyTypeForMethodPropertyShouldReturnType() throws Exception {
		// WHEN
		Object result = PropertyUtils.getPropertyType(Bean.class, "methodProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void getPropertyTypeForSetShouldReturnGenericType() throws Exception {
		// WHEN
		Object result = PropertyUtils.getPropertyType(Bean.class, "setProperty");

		// THEN
		assertThat(ParameterizedType.class).isAssignableFrom(result.getClass());
	}


	@Test
	public void onBooleanWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanPropertyWithMutators(true);

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "booleanPropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onStringPublicWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.publicProperty = "value";

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "publicProperty");

		// THEN
		assertThat(result).isEqualTo("value");
	}

	@Test
	public void onStringPublicReturnStringClass() throws Exception {
		// WHEN
		Object result = PropertyUtils.getPropertyClass(Bean.class, "publicProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void onStringPublicReturnStringType() throws Exception {
		// WHEN
		Object result = PropertyUtils.getPropertyType(Bean.class, "publicProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void onBooleanPropertyWithMutatorsReturnBooleanClass() throws Exception {
		// WHEN
		Object result = PropertyUtils.getPropertyClass(Bean.class, "booleanPropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo(Boolean.class);
	}


	@Test
	public void onStringProtectedGetWithMutatorsShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "protectedProperty");
	}

	@Test
	public void onInheritedStringPrivateWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new ChildBean();
		bean.setPrivatePropertyWithMutators("value");

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "privatePropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo("value");
	}

	@Test
	public void onMethodAccessorOnlyShouldReturnValue() throws Exception {
		// GIVEN
		GetterTest bean = new GetterTest();

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "property");

		// THEN
		assertThat(result).isEqualTo("valueProperty");
	}

	@Test
	public void onListValueForSetPropertyShouldGetConverted() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "setProperty", Arrays.asList("4", "1", "3", "2"));

		// THEN
		// confirm the order has been preserved
		assertEquals(bean.getSetProperty(), new LinkedHashSet(Arrays.asList("4", "1", "3", "2")));
	}

	@Test
	public void onNullBeanSetShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.setProperty(null, "privatePropertyWithMutators", null);
	}

	@Test
	public void onNullFieldSetShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.setProperty(bean, null, null);
	}

	@Test
	public void onBooleanPrimitiveWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "booleanPrimitivePropertyWithMutators", true);

		// THEN
		assertThat(bean.isBooleanPrimitivePropertyWithMutators()).isEqualTo(true);
	}

	@Test
	public void onBooleanWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "booleanPropertyWithMutators", true);

		// THEN
		assertThat(bean.getBooleanPropertyWithMutators()).isEqualTo(true);
	}

	@Test
	public void onStringPublicWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "publicProperty", "value");

		// THEN
		assertThat(bean.publicProperty).isEqualTo("value");
	}

	@Test
	public void onStringProtectedSetWithMutatorsShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "protectedProperty", null);
	}

	@Test
	public void onInheritedStringPrivateWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new ChildBean();

		// WHEN
		PropertyUtils.setProperty(bean, "privatePropertyWithMutators", "value");

		// THEN
		assertThat(bean.getPrivatePropertyWithMutators()).isEqualTo("value");
	}

	@Test
	public void onDifferentFieldAndMutatorNamesShouldSetValue() throws Exception {
		// GIVEN
		SetterTest bean = new SetterTest();

		// WHEN
		PropertyUtils.setProperty(bean, "property", "value");

		// THEN
		assertThat(bean.getProperty()).isEqualTo("value");
	}

	@Test
	public void onNonExistingPropertyShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "nonExistingProperty");
	}

	@Test
	public void onFieldListShouldSetValue() throws Exception {
		// GIVEN
		FieldListTest bean = new FieldListTest();
		List<String> value = Collections.singletonList("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.property).isEqualTo(value);
	}

	@Test
	public void onFieldSetShouldSetValue() throws Exception {
		// GIVEN
		FieldSetTest bean = new FieldSetTest();
		Set<String> value = Collections.singleton("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.property).isEqualTo(value);
	}

	@Test
	public void onSetterListShouldSetValue() throws Exception {
		// GIVEN
		SetterListTest bean = new SetterListTest();
		List<String> value = Collections.singletonList("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onSetterSetShouldSetValue() throws Exception {
		// GIVEN
		SetterSetTest bean = new SetterSetTest();
		Set<String> value = Collections.singleton("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithSetterListShouldSetValue() throws Exception {
		// GIVEN
		FieldWithSetterListTest bean = new FieldWithSetterListTest();
		List<String> value = Collections.singletonList("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithSetterSetShouldSetValue() throws Exception {
		// GIVEN
		FieldWithSetterSetTest bean = new FieldWithSetterSetTest();
		Set<String> value = Collections.singleton("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithThrowingUncheckedExceptionGetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalStateException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "uncheckedExceptionalField");
	}

	@Test
	public void onFieldWithThrowingUncheckedExceptionSetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalStateException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "uncheckedExceptionalField", "value");
	}

	@Test
	public void onFieldWithThrowingCheckedExceptionGetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "PropertyException");
	}

	@Test
	public void onFieldWithThrowingCheckedExceptionSetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "checkedExceptionalField", "value");
	}

	@Test
	public void unknownPropertyThrowingException() throws Exception {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "attrThatDoesNotExist", "value");
	}

	@Test
	public void unknownPropertyClassThrowingException() throws Exception {
		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getPropertyClass(Bean.class, "attrThatDoesNotExist");
	}

	@Test
	public void unknownPropertyTypeThrowingException() throws Exception {
		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getPropertyType(Bean.class, "attrThatDoesNotExist");
	}

	@Test
	public void nullBeanResultsInNullValue() throws Exception {
		// GIVEN
		Bean bean = null;

		// WHEN
		Object result = PropertyUtils.getProperty(bean, Arrays.asList("publicProperty"));

		// THEN
		assertThat(result).isNull();
	}

	public static class Bean {

		public String publicProperty;
		private boolean booleanWithGetPrefix;
		private String privatePropertyWithMutators;
		private boolean booleanPrimitivePropertyWithMutators;
		private Boolean booleanPropertyWithMutators;
		private Set<String> setProperty;

		public boolean getBooleanWithGetPrefix() {
			return booleanWithGetPrefix;
		}

		public void setBooleanWithGetPrefix(boolean booleanWithGetPrefix) {
			this.booleanWithGetPrefix = booleanWithGetPrefix;
		}

		public Set<String> getSetProperty() {
			return setProperty;
		}

		public void setSetProperty(Set<String> setProperty) {
			this.setProperty = setProperty;
		}

		public String getPrivatePropertyWithMutators() {
			return privatePropertyWithMutators;
		}

		public void setPrivatePropertyWithMutators(@SuppressWarnings("SameParameterValue") String privatePropertyWithMutators) {
			this.privatePropertyWithMutators = privatePropertyWithMutators;
		}

		public boolean isBooleanPrimitivePropertyWithMutators() {
			return booleanPrimitivePropertyWithMutators;
		}

		public void setBooleanPrimitivePropertyWithMutators(
				@SuppressWarnings("SameParameterValue") boolean booleanPrimitivePropertyWithMutators) {
			this.booleanPrimitivePropertyWithMutators = booleanPrimitivePropertyWithMutators;
		}

		public Boolean getBooleanPropertyWithMutators() {
			return booleanPropertyWithMutators;
		}

		public void setBooleanPropertyWithMutators(@SuppressWarnings("SameParameterValue") Boolean booleanPropertyWithMutators) {
			this.booleanPropertyWithMutators = booleanPropertyWithMutators;
		}

		public String getUncheckedExceptionalField() {
			throw new IllegalStateException();
		}

		public void setUncheckedExceptionalField(String value) {
			throw new IllegalStateException();
		}

		public String getCheckedExceptionalField() throws IllegalAccessException {
			throw new IllegalAccessException();
		}

		public void setCheckedExceptionalField(String value) throws IllegalAccessException {
			throw new IllegalAccessException();
		}

		public String getMethodProperty() {
			return "noFieldsHere";
		}
	}

	private static class ChildBean extends Bean {

	}

	public static class GetterTest {

		public String getProperty() {
			return "valueProperty";
		}
	}

	public static class SetterTest {

		public String anotherProperty;

		public String getProperty() {
			return anotherProperty;
		}

		public void setProperty(String property) {
			anotherProperty = property;
		}
	}

	public static class FieldListTest {

		public List<String> property;
	}

	public static class FieldSetTest {

		public Set<String> property;
	}

	public static class SetterListTest {

		private List<String> property;

		public List<String> getProperty() {
			return property;
		}

		public void setProperty(List<String> property) {
			this.property = property;
		}
	}

	public static class SetterSetTest {

		private Set<String> property;

		public Set<String> getProperty() {
			return property;
		}

		public void setProperty(Set<String> property) {
			this.property = property;
		}
	}

	public static class FieldWithSetterListTest {

		public List<String> property;

		public List<String> getProperty() {
			return property;
		}

		public void setProperty(List<String> property) {
			this.property = property;
		}
	}

	public static class FieldWithSetterSetTest {

		public Set<String> property;

		public Set<String> getProperty() {
			return property;
		}

		public void setProperty(Set<String> property) {
			this.property = property;
		}
	}
}
