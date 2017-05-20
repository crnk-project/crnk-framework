package io.crnk.core.internal.resource;

import io.crnk.core.engine.internal.information.resource.ReflectionFieldAccessor;
import io.crnk.core.engine.internal.utils.PropertyException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ReflectionFieldAccessorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onNullBeanGetShouldThrowException() throws Exception {
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "privatePropertyWithMutator", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.getValue(null);
	}

	@Test
	public void onNullFieldNameShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		new ReflectionFieldAccessor(Bean.class, null, String.class);
	}

	@Test
	public void onNullFieldTypeShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		new ReflectionFieldAccessor(Bean.class, "a", null);
	}

	@Test
	public void onNullResourceClassShouldThrowException() throws Exception {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		new ReflectionFieldAccessor(null, "a", String.class);
	}

	@Test
	public void onBooleanPrimitiveWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanPrimitivePropertyWithMutators(true);
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "booleanPrimitivePropertyWithMutators", boolean.class);

		// WHEN
		Object result = accessor.getValue(bean);

		// THEN
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void methodPropertyShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "methodProperty", String.class);

		// WHEN
		Object result = accessor.getValue(bean);

		// THEN
		assertThat(result).isEqualTo("noFieldsHere");
	}

	@Test
	public void onBooleanWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanPropertyWithMutators(true);
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "booleanPropertyWithMutators", Boolean.class);

		// WHEN
		Object result = accessor.getValue(bean);

		// THEN
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onStringPublicWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		bean.publicProperty = "value";
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "publicProperty", String.class);

		// WHEN
		Object result = accessor.getValue(bean);

		// THEN
		assertThat(result).isEqualTo("value");
	}

	@Test
	public void onInheritedStringPrivateWithMutatorsShouldReturnValue() throws Exception {
		// GIVEN
		Bean bean = new ChildBean();
		bean.setPrivatePropertyWithMutators("value");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(ChildBean.class, "privatePropertyWithMutators", String.class);

		// WHEN
		Object result = accessor.getValue(bean);

		// THEN
		assertThat(result).isEqualTo("value");
	}

	@Test
	public void onMethodAccessorOnlyShouldReturnValue() throws Exception {
		// GIVEN
		GetterTest bean = new GetterTest();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(GetterTest.class, "property", String.class);

		// WHEN
		Object result = accessor.getValue(bean);

		// THEN
		assertThat(result).isEqualTo("valueProperty");
	}

	@Test
	public void onListValueForSetPropertyShouldGetConverted() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "setProperty", Set.class);

		// WHEN
		accessor.setValue(bean, Arrays.asList("4", "1", "3", "2"));

		// THEN
		// confirm the order has been preserved
		assertEquals(bean.getSetProperty(), new LinkedHashSet(Arrays.asList("4", "1", "3", "2")));
	}

	@Test
	public void onBooleanPrimitiveWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "booleanPrimitivePropertyWithMutators", boolean.class);

		// WHEN
		accessor.setValue(bean, true);

		// THEN
		assertThat(bean.isBooleanPrimitivePropertyWithMutators()).isEqualTo(true);
	}

	@Test
	public void onBooleanWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "booleanPropertyWithMutators", Boolean.class);

		// WHEN
		accessor.setValue(bean, true);

		// THEN
		assertThat(bean.getBooleanPropertyWithMutators()).isEqualTo(true);
	}

	@Test
	public void onStringPublicWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "publicProperty", String.class);

		// WHEN
		accessor.setValue(bean, "value");

		// THEN
		assertThat(bean.publicProperty).isEqualTo("value");
	}

	@Test
	public void onInheritedStringPrivateWithMutatorsShouldSetValue() throws Exception {
		// GIVEN
		Bean bean = new ChildBean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(ChildBean.class, "privatePropertyWithMutators", String.class);

		// WHEN
		accessor.setValue(bean, "value");

		// THEN
		assertThat(bean.getPrivatePropertyWithMutators()).isEqualTo("value");
	}

	@Test
	public void onDifferentFieldAndMutatorNamesShouldSetValue() throws Exception {
		// GIVEN
		SetterTest bean = new SetterTest();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(SetterTest.class, "property", String.class);

		// WHEN
		accessor.setValue(bean, "value");

		// THEN
		assertThat(bean.getProperty()).isEqualTo("value");
	}

	@Test
	public void onNonExistingPropertyShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "nonExistingProperty", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.getValue(bean);
	}

	@Test
	public void onFieldListShouldSetValue() throws Exception {
		// GIVEN
		FieldListTest bean = new FieldListTest();
		List<String> value = Collections.singletonList("asd");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(FieldListTest.class, "property", List.class);

		// WHEN
		accessor.setValue(bean, value);

		// THEN
		assertThat(bean.property).isEqualTo(value);
	}

	@Test
	public void onFieldSetShouldSetValue() throws Exception {
		// GIVEN
		FieldSetTest bean = new FieldSetTest();
		Set<String> value = Collections.singleton("asd");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(FieldSetTest.class, "property", Set.class);

		// WHEN
		accessor.setValue(bean, value);

		// THEN
		assertThat(bean.property).isEqualTo(value);
	}

	@Test
	public void onSetterListShouldSetValue() throws Exception {
		// GIVEN
		SetterListTest bean = new SetterListTest();
		List<String> value = Collections.singletonList("asd");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(SetterListTest.class, "property", List.class);

		// WHEN
		accessor.setValue(bean, value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onSetterSetShouldSetValue() throws Exception {
		// GIVEN
		SetterSetTest bean = new SetterSetTest();
		Set<String> value = Collections.singleton("asd");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(SetterSetTest.class, "property", Set.class);

		// WHEN
		accessor.setValue(bean, value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithSetterListShouldSetValue() throws Exception {
		// GIVEN
		FieldWithSetterListTest bean = new FieldWithSetterListTest();
		List<String> value = Collections.singletonList("asd");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(FieldWithSetterListTest.class, "property", List.class);

		// WHEN
		accessor.setValue(bean, value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithSetterSetShouldSetValue() throws Exception {
		// GIVEN
		FieldWithSetterSetTest bean = new FieldWithSetterSetTest();
		Set<String> value = Collections.singleton("asd");
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(FieldWithSetterSetTest.class, "property", Set.class);

		// WHEN
		accessor.setValue(bean, value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithThrowingUncheckedExceptionGetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "uncheckedExceptionalField", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.getValue(bean);
	}

	@Test
	public void onFieldWithThrowingUncheckedExceptionSetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "uncheckedExceptionalField", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.setValue(bean, "value");
	}

	@Test
	public void onFieldWithThrowingCheckedExceptionGetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "checkedExceptionalField", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.getValue(bean);
	}

	@Test
	public void onFieldWithThrowingCheckedExceptionSetterShouldThrowException() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "checkedExceptionalField", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.setValue(bean, "value");
	}

	@Test
	public void unknownPropertyThrowingException() throws Exception {
		// GIVEN
		Bean bean = new Bean();
		ReflectionFieldAccessor accessor = new ReflectionFieldAccessor(Bean.class, "attrThatDoesNotExist", String.class);

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		accessor.setValue(bean, "value");
	}

	public static class Bean {
		public String publicProperty;
		private String privatePropertyWithMutators;
		private boolean booleanPrimitivePropertyWithMutators;
		private Boolean booleanPropertyWithMutators;
		private Set<String> setProperty;

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

		public void setBooleanPrimitivePropertyWithMutators(@SuppressWarnings("SameParameterValue") boolean booleanPrimitivePropertyWithMutators) {
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
