package io.crnk.core.resource.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiToMany;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceFieldTest {


	@Test
	public void testResourceIdEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(ResourceFieldImpl.class).suppress(Warning.NONFINAL_FIELDS, Warning.REFERENCE_EQUALITY).withPrefabValues(ResourceInformation.class, Mockito.mock
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

	/* FIXME
	@Test
	public void onWithLazyFieldClassShouldReturnTrue() throws Exception {
		// GIVEN
		List<Annotation> annotations = Arrays.asList(WithLazyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new ResourceFieldImpl("", "", String.class, String.class, null,
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
				new ResourceFieldImpl("", "", String.class, String.class, null,
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
				new ResourceFieldImpl("", "", String.class, String.class, null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}

	@Test
	public void checkToStringWithoutParent() throws Exception {
		List<Annotation> annotations = Arrays.asList(WithoutToManyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new ResourceFieldImpl("test", "test", String.class, String.class,
						null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		Assert.assertEquals("[jsonName=test]", sut.toString());
	}

	@Test
	public void checkToStringWithParent() throws Exception {
		List<Annotation> annotations = Arrays.asList(WithoutToManyFieldClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new ResourceFieldImpl("test", "test", String.class, String.class,
						null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		ResourceInformation parent = Mockito.mock(ResourceInformation.class);
		Mockito.when(parent.getResourceType()).thenReturn("type");
		Mockito.when(parent.toString()).thenReturn("parent");
		Mockito.when(parent.getResourceClass()).thenReturn((Class) Task.class);
		sut.setResourceInformation(parent);

		Assert.assertEquals("[jsonName=test,resourceType=parent]", sut.toString());
	}


	@Test
	public void onLazyRelationshipToManyAndInclusionByDefaultShouldReturnEagerFlag() throws Exception {
		// GIVEN
		List<Annotation> annotations =
				Arrays.asList(WithLazyFieldAndInclusionByDefaultClass.class.getDeclaredField("value").getAnnotations());
		ResourceField sut =
				new ResourceFieldImpl("", "", String.class, String.class, null,
						annotations, new ResourceFieldAccess(true, true, true, true));

		// WHEN
		boolean result = sut.isLazy();

		// THEN

		assertThat(result).isFalse();
	}
*/


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
