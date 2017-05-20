package io.crnk.core.resource.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceFieldNameTransformerTest {

	private ObjectMapper objectMapper;
	private ResourceFieldNameTransformer sut;

	@Before
	public void setUp() throws Exception {
		objectMapper = new ObjectMapper();
		sut = new ResourceFieldNameTransformer(objectMapper.getSerializationConfig());
	}

	@Test
	public void onFieldWithoutJsonPropertyShouldReturnBaseName() throws Exception {
		// GIVEN
		Field field = TestClass.class.getDeclaredField("field");

		// WHEN
		String name = sut.getName(field);

		// THEN
		assertThat(name).isEqualTo("field");
	}

	@Test
	public void onFieldWithJsonPropertyShouldReturnCustomName() throws Exception {
		// GIVEN
		Field field = TestClass.class.getDeclaredField("fieldWithJsonProperty");

		// WHEN
		String name = sut.getName(field);

		// THEN
		assertThat(name).isEqualTo("customName");
	}

	@Test
	public void onFieldWithDefaultJsonPropertyShouldReturnBaseName() throws Exception {
		// GIVEN
		Field field = TestClass.class.getDeclaredField("fieldWithDefaultJsonProperty");

		// WHEN
		String name = sut.getName(field);

		// THEN
		assertThat(name).isEqualTo("fieldWithDefaultJsonProperty");
	}

	@Test
	public void onWrappedBooleanFieldShouldReturnFieldNameBasedOnGetter() throws Exception {
		// GIVEN
		Method method = TestClass.class.getDeclaredMethod("getAccessorField");

		// WHEN
		String name = sut.getName(method);

		// THEN
		assertThat(name).isEqualTo("accessorField");
	}

	@Test
	public void onWrappedFieldShouldReturnFieldNameBasedOnGetter() throws Exception {
		// GIVEN
		Method method = TestClass.class.getDeclaredMethod("isBooleanProperty");

		// WHEN
		String name = sut.getName(method);

		// THEN
		assertThat(name).isEqualTo("booleanProperty");
	}

	@Test
	public void onAnnotatedWrappedFieldShouldReturnFieldNameBasedOnAnnotation() throws Exception {
		// GIVEN
		Method method = TestClass.class.getDeclaredMethod("getAccessorFieldWithAnnotation");

		// WHEN
		String name = sut.getName(method);

		// THEN
		assertThat(name).isEqualTo("wrappedCustomName");
	}

	@Test
	public void onNoSerializationConfigShouldSerializeField() throws Exception {
		// GIVEN
		sut = new ResourceFieldNameTransformer();
		Field field = TestClass.class.getDeclaredField("namingStrategyTest");

		// WHEN
		String name = sut.getName(field);

		// THEN
		assertThat(name).isEqualTo("namingStrategyTest");
	}

	@Test
	public void onNoSerializationConfigShouldSerializeMethod() throws Exception {
		// GIVEN
		sut = new ResourceFieldNameTransformer();
		Method method = TestClass.class.getDeclaredMethod("getAccessorField");

		// WHEN
		String name = sut.getName(method);

		// THEN
		assertThat(name).isEqualTo("accessorField");
	}

	@Test
	public void onMethodNameWithNamingStrategyShouldReturnModifiedName() throws Exception {
		// GIVEN
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		sut = new ResourceFieldNameTransformer(objectMapper.getSerializationConfig());
		Method method = TestClass.class.getDeclaredMethod("getAccessorField");

		// WHEN
		String name = sut.getName(method);

		// THEN
		assertThat(name).isEqualTo("accessor_field");
	}

	@Test
	public void onFieldNameWithNamingStrategyShouldReturnModifiedName() throws Exception {
		// GIVEN
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		sut = new ResourceFieldNameTransformer(objectMapper.getSerializationConfig());
		Field field = TestClass.class.getDeclaredField("namingStrategyTest");

		// WHEN
		String name = sut.getName(field);

		// THEN
		assertThat(name).isEqualTo("naming_strategy_test");
	}

	private static class TestClass {

		private String field;
		private String namingStrategyTest;
		@JsonProperty("customName")
		private String fieldWithJsonProperty;
		@JsonProperty
		private String fieldWithDefaultJsonProperty;

		public String getAccessorField() {
			return null;
		}

		@JsonProperty("wrappedCustomName")
		public String getAccessorFieldWithAnnotation() {
			return null;
		}

		private boolean isBooleanProperty() {
			return false;
		}
	}
}
