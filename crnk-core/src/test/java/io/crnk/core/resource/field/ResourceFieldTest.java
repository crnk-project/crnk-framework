package io.crnk.core.resource.field;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiToMany;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourceFieldTest {


	@Test
	public void testResourceIdEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(ResourceFieldImpl.class).withPrefabValues(ResourceInformation.class, Mockito.mock
				(ResourceInformation.class), Mockito.mock(ResourceInformation.class)
		).usingGetClass().verify();
	}

	@Test
	public void getResourceFieldType() {
		assertThat(ResourceFieldType.get(true, false, false, false)).isEqualByComparingTo(ResourceFieldType.ID);
		assertThat(ResourceFieldType.get(false, true, false, false)).isEqualByComparingTo(ResourceFieldType.LINKS_INFORMATION);
		assertThat(ResourceFieldType.get(false, false, true, false)).isEqualByComparingTo(ResourceFieldType.META_INFORMATION);
		assertThat(ResourceFieldType.get(false, false, false, true)).isEqualByComparingTo(ResourceFieldType.RELATIONSHIP);
		assertThat(ResourceFieldType.get(false, false, false, false)).isEqualByComparingTo(ResourceFieldType.ATTRIBUTE);
	}

	@Test
	public void onWithLazyFieldClassShouldReturnTrue() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithLazyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isTrue();
	}

	@Test
	public void onWithToManyEagerFieldClassShouldReturnFalse() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithToManyEagerFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}

	@Test
	public void onWithoutToManyFieldClassShouldReturnFalse() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithoutToManyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}

	@Test
	public void onLazyRelationshipToManyAndInclusionByDefaultShouldReturnEagerFlag() throws Exception {
		// GIVEN
		List<Annotation> annotations =
				Arrays.asList(WithLazyFieldAndInclusionByDefaultClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new AnnotationResourceInformationBuilder.AnnotatedResourceField("", "", String.class, String.class, null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}

	private static class WithLazyFieldClass {

		@JsonProperty("sth")
		@JsonApiToMany
		private String value;
	}

	private static class WithLazyFieldAndInclusionByDefaultClass {

		@JsonApiIncludeByDefault
		@JsonApiToMany
		private String value;
	}

	private static class WithToManyEagerFieldClass {

		@JsonApiToMany(lazy = false)
		private String value;
	}

	private static class WithoutToManyFieldClass {

		private String value;

	}
}
