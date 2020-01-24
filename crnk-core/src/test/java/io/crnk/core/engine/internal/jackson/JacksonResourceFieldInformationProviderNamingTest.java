package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.information.resource.AnnotatedClassBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotatedFieldBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotatedMethodBuilder;
import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonResourceFieldInformationProviderNamingTest {

	private ObjectMapper objectMapper;

	private JacksonResourceFieldInformationProvider sut;

	private ResourceInformationProviderContext context;

	private BeanInformation beanDesc;

	@Before
	public void setUp() {
		objectMapper = new ObjectMapper();

		context = Mockito.mock(ResourceInformationProviderContext.class);
		Mockito.when(context.getObjectMapper()).thenReturn(objectMapper);

		sut = new JacksonResourceFieldInformationProvider();
		sut.init(context);

		beanDesc = BeanInformation.get(TestClass.class);
	}

	@Test
	public void checkUtilConstructors() {
		CoreClassTestUtils.assertPrivateConstructor(AnnotatedClassBuilder.class);
		CoreClassTestUtils.assertPrivateConstructor(AnnotatedFieldBuilder.class);
		CoreClassTestUtils.assertPrivateConstructor(AnnotatedMethodBuilder.class);
	}

	@Test
	public void onFieldWithoutJsonPropertyShouldReturnBaseName() {
		BeanAttributeInformation field = beanDesc.getAttribute("field");
		assertThat(sut.getJsonName(field).isPresent()).isFalse();
	}

	@Test
	public void onFieldWithJsonPropertyShouldReturnCustomName() {
		// GIVEN
		BeanAttributeInformation field = beanDesc.getAttribute("fieldWithJsonProperty");

		// WHEN
		String name = sut.getJsonName(field).get();

		// THEN
		assertThat(name).isEqualTo("customName");
	}

	@Test
	public void onFieldWithDefaultJsonPropertyShouldReturnBaseName() {
		BeanAttributeInformation attr = beanDesc.getAttribute("fieldWithDefaultJsonProperty");
		assertThat(sut.getJsonName(attr).isPresent()).isFalse();
	}

	@Test
	public void onWrappedBooleanFieldShouldReturnFieldNameBasedOnGetter() {
		BeanAttributeInformation attr = beanDesc.getAttribute("accessorField");
		assertThat(sut.getJsonName(attr).isPresent()).isFalse();
	}

	@Test
	public void onWrappedFieldShouldReturnFieldNameBasedOnGetter() {
		BeanAttributeInformation attr = beanDesc.getAttribute("booleanProperty");
		assertThat(sut.getJsonName(attr).isPresent()).isFalse();
	}

	@Test
	public void onNoSerializationConfigShouldSerializeField() {
		sut = new JacksonResourceFieldInformationProvider();
		sut.init(context);
		BeanAttributeInformation field = beanDesc.getAttribute("namingStrategyTest");
		assertThat(sut.getJsonName(field).isPresent()).isFalse();
	}

	@Test
	public void onNoSerializationConfigShouldSerializeMethod() throws Exception {
		sut = new JacksonResourceFieldInformationProvider();
		sut.init(context);
		Method method = TestClass.class.getDeclaredMethod("getAccessorField");
		assertThat(sut.getName(method).isPresent()).isFalse();

		BeanAttributeInformation attr = beanDesc.getAttribute("accessorField");
		assertThat(sut.getJsonName(attr).isPresent()).isFalse();
	}

	@Test
	public void onMethodNameWithNamingStrategyShouldReturnModifiedName() throws Exception {
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		sut = new JacksonResourceFieldInformationProvider();
		sut.init(context);
		Method method = TestClass.class.getDeclaredMethod("getAccessorField");
		String name = sut.getName(method).get();
		assertThat(name).isEqualTo("accessor_field");

		BeanAttributeInformation attr = beanDesc.getAttribute("accessorField");
		assertThat(sut.getJsonName(attr).get()).isEqualTo("accessor_field");
	}

	@Test
	public void onFieldNameWithNamingStrategyShouldReturnModifiedName() throws Exception {
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		sut = new JacksonResourceFieldInformationProvider();
		sut.init(context);
		Field field = TestClass.class.getDeclaredField("namingStrategyTest");
		String name = sut.getName(field).get();
		assertThat(name).isEqualTo("naming_strategy_test");

		BeanAttributeInformation attr = beanDesc.getAttribute("namingStrategyTest");
		assertThat(sut.getJsonName(attr).get()).isEqualTo("naming_strategy_test");
	}

	private static class TestClass {

		public String field;

		public String namingStrategyTest;

		@JsonProperty("customName")
		public String fieldWithJsonProperty;

		@JsonProperty
		public String fieldWithDefaultJsonProperty;

		public String getAccessorField() {
			return null;
		}

		@JsonProperty("wrappedCustomName")
		public String getAccessorFieldWithAnnotation() {
			return null;
		}

		public boolean isBooleanProperty() {
			return false;
		}
	}
}
