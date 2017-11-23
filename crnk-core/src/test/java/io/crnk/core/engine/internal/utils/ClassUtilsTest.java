package io.crnk.core.engine.internal.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.repository.ResourceRepository;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ClassUtilsTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void hasPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(ClassUtils.class);
	}

	@Test
	public void stringMustExist() {
		Assert.assertTrue(ClassUtils.existsClass(String.class.getName()));
	}

	@Test
	public void unknownClassMustNotExist() {
		Assert.assertFalse(ClassUtils.existsClass("does.not.exist"));
	}

	@Test
	public void rawTypeFromParameterizedType() throws Exception {
		// WHEN
		Class<?> result = ClassUtils.getRawType(ProjectRepository.class.getGenericInterfaces()[0]);

		// THEN
		assertThat(result).isEqualTo(ResourceRepository.class);
	}

	@Test
	public void rawTypeFromRawType() throws Exception {
		// WHEN
		Class<?> result = ClassUtils.getRawType(String.class);

		// THEN
		assertThat(result).isEqualTo(String.class);
	}

	@Test(expected = IllegalStateException.class)
	public void exceptionForUnknownRawType() throws Exception {
		ClassUtils.getRawType(null);
	}

	@Test
	public void onClassInheritanceShouldReturnInheritedClasses() throws Exception {
		// WHEN
		List<Field> result = ClassUtils.getClassFields(ChildClass.class);

		// THEN
		assertThat(result).hasSize(2);
	}

	@Test
	public void onClassInheritanceShouldReturnInheritedField() throws Exception {
		// WHEN
		Field result = ClassUtils.findClassField(ChildClass.class, "parentField");

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("parentField");
		assertThat(result.getDeclaringClass()).isEqualTo(ParentClass.class);
	}

	@Test
	public void onGetGettersShouldReturnMethodsStartingWithGet() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

		// THEN
		assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("aetParentField"));
	}

	@Test
	public void onGetGettersShouldReturnMethodsThatNotTakeParams() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

		// THEN
		assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("getParentFieldWithParameter", String.class));
	}

	@Test
	public void onGetGettersShouldReturnMethodsThatReturnValue() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

		// THEN
		assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("getParentFieldReturningVoid"));
	}

	@Test
	public void onGetGettersShouldReturnBooleanGettersThatHaveName() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

		// THEN
		assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("is"));
	}

	@Test
	public void onGetGettersShouldReturnNonBooleanGettersThatHaveName() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

		// THEN
		assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("get"));
	}

	@Test
	public void onClassInheritanceShouldReturnInheritedGetters() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassGetters(ChildClass.class);

		// THEN
		assertThat(result).hasSize(5);
	}

	@Test
	public void onFindGetterShouldReturnBooleanPropertyWithGet() throws Exception {
		Method method = ClassUtils.findGetter(ParentClass.class, "booleanPropertyWithGet");
		assertThat(method.getName()).isEqualTo("getBooleanPropertyWithGet");
	}

	@Test
	public void onGetSettersShouldReturnMethodsThatSetValue() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassSetters(ParentClass.class);

		// THEN
		assertThat(result).containsOnly(ParentClass.class.getDeclaredMethod("setValue", String.class));
	}

	@Test
	public void onClassInheritanceShouldReturnInheritedSetters() throws Exception {
		// WHEN
		List<Method> result = ClassUtils.getClassSetters(ChildClass.class);

		// THEN
		assertThat(result).hasSize(1);
	}

	@Test
	public void onGetAnnotationShouldReturnAnnotation() {
		// WHEN
		Optional<JsonApiResource> result = ClassUtils.getAnnotation(ResourceClass.class, JsonApiResource.class);

		// THEN
		assertThat(result.get()).isInstanceOf(JsonApiResource.class);
	}

	@Test
	public void onGetAnnotationShouldReturnParentAnnotation() {
		// WHEN
		Optional<JsonPropertyOrder> result = ClassUtils.getAnnotation(ChildClass.class, JsonPropertyOrder.class);

		// THEN
		assertThat(result.get()).isInstanceOf(JsonPropertyOrder.class);
	}

	@Test
	public void onNonExistingAnnotationShouldReturnEmptyResult() {
		// WHEN
		Optional<JsonIgnore> result = ClassUtils.getAnnotation(ResourceClass.class, JsonIgnore.class);

		// THEN
		assertThat(result.isPresent()).isFalse();
	}

	@Test
	public void onValidClassShouldCreateNewInstance() throws Exception {
		// WHEN
		ResourceClass result = ClassUtils.newInstance(ResourceClass.class);

		// THEN
		assertThat(result).isInstanceOf(ResourceClass.class);
	}

	@Test
	public void ignoreSyntheticMethods() throws Exception {
		// WHEN
		List<Method> getterMethods = ClassUtils.getClassGetters(IntegerClass.class);
		List<Method> setterMethods = ClassUtils.getClassSetters(IntegerClass.class);

		// THEN
		assertEquals(1, getterMethods.size());
		assertEquals(Integer.class, getterMethods.get(0).getReturnType());
		assertEquals(1, setterMethods.size());

	}

	@Test(expected = IllegalStateException.class)
	public void onClassWithCrushingConstructorShouldThrowException() throws Exception {
		// WHEN
		ClassUtils.newInstance(ClassWithCrashingConstructor.class);
	}

	@Test(expected = ResourceException.class)
	public void onClassWithoutDefaultConstructorShouldThrowException() throws Exception {
		// WHEN
		ClassUtils.newInstance(ClassWithoutDefaultConstructor.class);
	}

	@Test
	public void onFindGetterShouldReturnIntegerMethod() throws Exception {
		Method method = ClassUtils.findGetter(IntegerClass.class, "id");
		assertThat(method.getName()).isEqualTo("getId");
	}

	@Test
	public void onFindSetterShouldReturnIntegerMethod() throws Exception {
		Method method = ClassUtils.findSetter(IntegerClass.class, "id", Integer.class);
		assertThat(method.getName()).isEqualTo("setId");
	}

	@Test
	public void onFindGetterShouldReturnPrimitiveBooleanMethod() throws Exception {
		Method method = ClassUtils.findGetter(ParentClass.class, "primitiveBooleanProperty");
		assertThat(method.getName()).isEqualTo("isPrimitiveBooleanProperty");
	}

	@Test
	public void onFindGetterShouldReturnBooleanMethod() throws Exception {
		Method method = ClassUtils.findGetter(ParentClass.class, "booleanProperty");
		assertThat(method.getName()).isEqualTo("isBooleanProperty");
	}

	@Test
	public void onFindGetterShouldNotReturnNonBooleanIsMethods() throws Exception {
		Method method = ClassUtils.findGetter(InvalidBooleanClass.class, "notABooleanReturnType");
		assertThat(method).isNull();
	}

	@Test
	public void testIsPrimitiveType() {
		assertThat(ClassUtils.isPrimitiveType(boolean.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(byte.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(short.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(int.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(long.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(short.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(double.class)).isTrue();
		assertThat(ClassUtils.isPrimitiveType(String.class)).isFalse();
		assertThat(ClassUtils.isPrimitiveType(Object.class)).isFalse();
		assertThat(ClassUtils.isPrimitiveType(int[].class)).isFalse();
	}

	@JsonPropertyOrder(alphabetic = true)
	public static class ParentClass {

		private String parentField;

		public String getParentField() {
			return parentField;
		}

		public String aetParentField() {
			return parentField;
		}

		public String getParentFieldWithParameter(String parameter) {
			return parentField;
		}

		public void getParentFieldReturningVoid() {
		}

		public void setValue(String value) {

		}

		public void setValueWithoutParameter() {

		}

		public boolean isPrimitiveBooleanProperty() {
			return true;
		}

		public Boolean isBooleanProperty() {
			return true;
		}

		public boolean getBooleanPropertyWithGet() {
			return true;
		}

		public boolean is() {
			return true;
		}

		public String get() {
			return "value";
		}

		public void set(String value) {
		}
	}

	public static class ChildClass extends ParentClass {

		private String childField;

		public String getChildField() {
			return childField;
		}
	}

	@JsonApiResource(type = "document")
	public static class ResourceClass {

	}

	public static class ClassWithCrashingConstructor {

		public ClassWithCrashingConstructor() {
			throw new IllegalStateException();
		}
	}

	public static class ClassWithoutDefaultConstructor {

		public ClassWithoutDefaultConstructor(String value) {
		}
	}

	private abstract class BaseGenericClass<T> {

		public abstract T getId();

		public abstract void setId(T id);

	}

	private class InvalidBooleanClass {

		public int isNotABooleanReturnType() {
			return 12;
		}
	}

	private class IntegerClass extends BaseGenericClass<Integer> {

		private Integer id = 1;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

	}
}
