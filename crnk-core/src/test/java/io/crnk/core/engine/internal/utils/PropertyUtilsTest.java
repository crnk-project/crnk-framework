package io.crnk.core.engine.internal.utils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class PropertyUtilsTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onNullBeanGetShouldThrowException() {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.getProperty(null, "privatePropertyWithMutators");
	}

	@Test
	public void onBooleanWithGetPrefix() {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanWithGetPrefix(true);

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "booleanWithGetPrefix");

		// THEN
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onNullFieldGetShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.getProperty(bean, (String) null);
	}

	@Test
	public void onBooleanPrimitiveWithMutatorsShouldReturnValue() {
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
	public void methodPropertyShouldReturnValue() {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		Object result = PropertyUtils
				.getProperty(bean, "methodProperty");

		// THEN
		assertThat(result).isEqualTo("noFieldsHere");
	}

	@Test
	public void getPropertyClassForMethodPropertyShouldReturnClass() {
		// WHEN
		Object result = PropertyUtils.getPropertyClass(Bean.class, "methodProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void getPropertyTypeForMethodPropertyShouldReturnType() {
		// WHEN
		Object result = PropertyUtils.getPropertyType(Bean.class, "methodProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void getPropertyTypeForSetShouldReturnGenericType() {
		// WHEN
		Object result = PropertyUtils.getPropertyType(Bean.class, "setProperty");

		// THEN
		assertThat(ParameterizedType.class).isAssignableFrom(result.getClass());
	}


	@Test
	public void onBooleanWithMutatorsShouldReturnValue() {
		// GIVEN
		Bean bean = new Bean();
		bean.setBooleanPropertyWithMutators(true);

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "booleanPropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void onStringPublicWithMutatorsShouldReturnValue() {
		// GIVEN
		Bean bean = new Bean();
		bean.publicProperty = "value";

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "publicProperty");

		// THEN
		assertThat(result).isEqualTo("value");
	}

	@Test
	public void onStringPublicReturnStringClass() {
		// WHEN
		Object result = PropertyUtils.getPropertyClass(Bean.class, "publicProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void getPropertyClassShouldThrowExceptionForInvalidField() {
		try {
			PropertyUtils.getPropertyClass(Bean.class, "doesNotExist");
			Assert.fail();
		} catch (PropertyException e) {
			Assert.assertEquals("doesNotExist", e.getField());
			Assert.assertEquals(Bean.class, e.getResourceClass());
		}
	}

	@Test
	public void getPropertyTypeShouldThrowExceptionForInvalidField() {
		try {
			PropertyUtils.getPropertyType(Bean.class, "doesNotExist");
			Assert.fail();
		} catch (PropertyException e) {
			Assert.assertEquals("doesNotExist", e.getField());
			Assert.assertEquals(Bean.class, e.getResourceClass());
		}
	}

	@Test
	public void onStringPublicReturnStringType() {
		// WHEN
		Object result = PropertyUtils.getPropertyType(Bean.class, "publicProperty");

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test
	public void onBooleanPropertyWithMutatorsReturnBooleanClass() {
		// WHEN
		Object result = PropertyUtils.getPropertyClass(Bean.class, "booleanPropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo(Boolean.class);
	}


	@Test
	public void onStringProtectedGetWithMutatorsShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "protectedProperty");
	}

	@Test
	public void onInheritedStringPrivateWithMutatorsShouldReturnValue() {
		// GIVEN
		Bean bean = new ChildBean();
		bean.setPrivatePropertyWithMutators("value");

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "privatePropertyWithMutators");

		// THEN
		assertThat(result).isEqualTo("value");
	}

	@Test
	public void onMethodAccessorOnlyShouldReturnValue() {
		// GIVEN
		GetterTest bean = new GetterTest();

		// WHEN
		Object result = PropertyUtils.getProperty(bean, "property");

		// THEN
		assertThat(result).isEqualTo("valueProperty");
	}

	@Test
	public void onListValueForSetPropertyShouldGetConverted() {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "setProperty", Arrays.asList("4", "1", "3", "2"));

		// THEN
		// confirm the order has been preserved
		assertEquals(bean.getSetProperty(), new LinkedHashSet(Arrays.asList("4", "1", "3", "2")));
	}

	@Test
	public void onNullBeanSetShouldThrowException() {
		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.setProperty(null, "privatePropertyWithMutators", null);
	}

	@Test
	public void onNullFieldSetShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalArgumentException.class);

		// WHEN
		PropertyUtils.setProperty(bean, null, null);
	}

	@Test
	public void onBooleanPrimitiveWithMutatorsShouldSetValue() {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "booleanPrimitivePropertyWithMutators", true);

		// THEN
		assertThat(bean.isBooleanPrimitivePropertyWithMutators()).isEqualTo(true);
	}

	@Test
	public void onBooleanWithMutatorsShouldSetValue() {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "booleanPropertyWithMutators", true);

		// THEN
		assertThat(bean.getBooleanPropertyWithMutators()).isEqualTo(true);
	}

	@Test
	public void onStringPublicWithMutatorsShouldSetValue() {
		// GIVEN
		Bean bean = new Bean();

		// WHEN
		PropertyUtils.setProperty(bean, "publicProperty", "value");

		// THEN
		assertThat(bean.publicProperty).isEqualTo("value");
	}

	@Test
	public void onStringProtectedSetWithMutatorsShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "protectedProperty", null);
	}

	@Test
	public void onInheritedStringPrivateWithMutatorsShouldSetValue() {
		// GIVEN
		Bean bean = new ChildBean();

		// WHEN
		PropertyUtils.setProperty(bean, "privatePropertyWithMutators", "value");

		// THEN
		assertThat(bean.getPrivatePropertyWithMutators()).isEqualTo("value");
	}

	@Test
	public void onDifferentFieldAndMutatorNamesShouldSetValue() {
		// GIVEN
		SetterTest bean = new SetterTest();

		// WHEN
		PropertyUtils.setProperty(bean, "property", "value");

		// THEN
		assertThat(bean.getProperty()).isEqualTo("value");
	}

	@Test
	public void onNonExistingPropertyShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "nonExistingProperty");
	}

	@Test
	public void onFieldListShouldSetValue() {
		// GIVEN
		FieldListTest bean = new FieldListTest();
		List<String> value = Collections.singletonList("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.property).isEqualTo(value);
	}

	@Test
	public void onFieldSetShouldSetValue() {
		// GIVEN
		FieldSetTest bean = new FieldSetTest();
		Set<String> value = Collections.singleton("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.property).isEqualTo(value);
	}

	@Test
	public void onSetterListShouldSetValue() {
		// GIVEN
		SetterListTest bean = new SetterListTest();
		List<String> value = Collections.singletonList("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onSetterSetShouldSetValue() {
		// GIVEN
		SetterSetTest bean = new SetterSetTest();
		Set<String> value = Collections.singleton("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithSetterListShouldSetValue() {
		// GIVEN
		FieldWithSetterListTest bean = new FieldWithSetterListTest();
		List<String> value = Collections.singletonList("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithSetterSetShouldSetValue() {
		// GIVEN
		FieldWithSetterSetTest bean = new FieldWithSetterSetTest();
		Set<String> value = Collections.singleton("asd");

		// WHEN
		PropertyUtils.setProperty(bean, "property", value);

		// THEN
		assertThat(bean.getProperty()).isEqualTo(value);
	}

	@Test
	public void onFieldWithThrowingUncheckedExceptionGetterShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalStateException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "uncheckedExceptionalField");
	}

	@Test
	public void onFieldWithThrowingUncheckedExceptionSetterShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(IllegalStateException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "uncheckedExceptionalField", "value");
	}

	@Test
	public void onFieldWithThrowingCheckedExceptionGetterShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getProperty(bean, "PropertyException");
	}

	@Test
	public void onFieldWithThrowingCheckedExceptionSetterShouldThrowException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "checkedExceptionalField", "value");
	}

	@Test
	public void unknownPropertyThrowingException() {
		// GIVEN
		Bean bean = new Bean();

		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.setProperty(bean, "attrThatDoesNotExist", "value");
	}

	@Test
	public void unknownPropertyClassThrowingException() {
		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getPropertyClass(Bean.class, "attrThatDoesNotExist");
	}

	@Test
	public void unknownPropertyTypeThrowingException() {
		// THEN
		expectedException.expect(PropertyException.class);

		// WHEN
		PropertyUtils.getPropertyType(Bean.class, "attrThatDoesNotExist");
	}

	@Test
	public void nullBeanResultsInNullValue() {
		// GIVEN
		Bean bean = null;

		// WHEN
		Object result = PropertyUtils.getProperty(bean, Arrays.asList("publicProperty"));

		// THEN
		assertThat(result).isNull();
	}

	@Test
	public void onMultipleNestedCollectionsShouldReturnValue() {
		// GIVEN
		Bean bean = new Bean();

		SubNestedBean subNestedBean = new SubNestedBean();
		subNestedBean.setProperty("propertyValue");

		NestedBean nestedBean = new NestedBean();
		nestedBean.setSubNestedBeans(Collections.singletonList(subNestedBean));

		bean.setNestedBeans(Collections.singletonList(nestedBean));

		// WHEN
		Object result = PropertyUtils.getProperty(bean, Arrays.asList("nestedBeans", "subNestedBeans", "property"));

		// THEN
		assertThat(result).isEqualTo(Collections.singletonList("propertyValue"));
	}

	public static class Bean {

		public String publicProperty;

		private boolean booleanWithGetPrefix;

		private String privatePropertyWithMutators;

		private boolean booleanPrimitivePropertyWithMutators;

		private Boolean booleanPropertyWithMutators;

		private Set<String> setProperty;

		private Collection<NestedBean> nestedBeans;

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

		public Collection<NestedBean> getNestedBeans() {
			return nestedBeans;
		}

		public void setNestedBeans(Collection<NestedBean> nestedBeans) {
			this.nestedBeans = nestedBeans;
		}
	}

	private static class ChildBean extends Bean {

	}

	private static class NestedBean {

		private Collection<SubNestedBean> subNestedBeans;

		public Collection<SubNestedBean> getSubNestedBeans() {
			return subNestedBeans;
		}

		public void setSubNestedBeans(
				Collection<SubNestedBean> subNestedBeans) {
			this.subNestedBeans = subNestedBeans;
		}
	}

	private static class SubNestedBean {

		private String property;

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}
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
