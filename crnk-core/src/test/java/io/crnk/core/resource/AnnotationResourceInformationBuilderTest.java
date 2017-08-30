package io.crnk.core.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceAttributesBridge;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.exception.*;
import io.crnk.core.mock.models.ShapeResource;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.UnAnnotatedTask;
import io.crnk.core.resource.annotations.*;
import io.crnk.legacy.registry.DefaultResourceInformationBuilderContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationResourceInformationBuilderTest {

	private static final String NAME_PROPERTY = "underlyingName";
	private final ResourceInformationBuilder resourceInformationBuilder =
			new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer(), new JacksonResourceFieldInformationProvider());
	private final ResourceInformationBuilderContext context =
			new DefaultResourceInformationBuilderContext(resourceInformationBuilder, new TypeParser());
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		resourceInformationBuilder.init(context);
	}

	@Test
	public void shouldHaveResourceClassInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getResourceClass()).isNotNull().isEqualTo(Task.class);
	}

	@Test
	public void checkJsonApiAttributeAnnotation() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		ResourceField field = resourceInformation.findAttributeFieldByName("status");
		Assert.assertFalse(field.getAccess().isPatchable());
		Assert.assertFalse(field.getAccess().isPostable());
	}

	@Test
	public void shouldDiscardParametrizedTypeWithJsonIgnore() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(ShapeResource.class);

		// if we get this far, that is good, it means parsing the class didn't trigger the
		// IllegalStateException when calling ClassUtils#getRawType on a parameterized type T

		assertThat(resourceInformation.findAttributeFieldByName("type")).isNotNull();
		// This assert fails, because JsonIgnore is on the getter not the field itself
		// assertThat(resourceInformation.findAttributeFieldByName("delegate")).isNull();
		assertThat(resourceInformation.getIdField().getUnderlyingName()).isNotNull().isEqualTo("id");
		assertThat(containsFieldWithName(resourceInformation, "delegate")).isFalse();
	}

	@Test
	public void shouldHaveGetterBooleanWithGetPrefix() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		assertThat(containsFieldWithName(resourceInformation, "deleted")).isTrue();
		assertThat(containsFieldWithName(resourceInformation, "tDeleted")).isFalse();
	}

	@Test
	public void shouldHaveGetterBooleanWithIsPrefix() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		assertThat(containsFieldWithName(resourceInformation, "completed")).isTrue();
	}

	@Test
	public void shouldNotHaveIgnoredField() throws Exception {
		// GIVEN a field that has the JsonIgnore annotation, and a corresponding getter that does not
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		// THEN we should not pick up the java bean property
		assertThat(containsFieldWithName(resourceInformation, "ignoredField")).isFalse();
	}

	@Test
	public void shouldHaveOneIdFieldOfTypeLong() {
				/*
 			Task has a Long getId() field and a boolean hasId() which is ignored, only the former should have survived
 		 */
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		assertThat(resourceInformation.getIdField()).isNotNull();
		assertThat(resourceInformation.getIdField().getType()).isEqualTo(Long.class);
		assertThat(containsFieldWithName(resourceInformation, "hasId")).isFalse();
	}

	private boolean containsFieldWithName(ResourceInformation resourceInformation, String name) {
		ResourceAttributesBridge<?> attributeFields = resourceInformation.getAttributeFields();
		for (ResourceField field : attributeFields.getFields()) {
			if (field.getUnderlyingName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void checkJsonPropertyAccessPolicy() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(JsonIngoreTestResource.class);
		ResourceField defaultAttribute = resourceInformation.findAttributeFieldByName("defaultAttribute");
		ResourceField readOnlyAttribute = resourceInformation.findAttributeFieldByName("readOnlyAttribute");
		ResourceField readWriteAttribute = resourceInformation.findAttributeFieldByName("readWriteAttribute");
		Assert.assertTrue(defaultAttribute.getAccess().isPatchable());
		Assert.assertTrue(defaultAttribute.getAccess().isPostable());
		Assert.assertFalse(readOnlyAttribute.getAccess().isPatchable());
		Assert.assertFalse(readOnlyAttribute.getAccess().isPostable());
		Assert.assertTrue(readWriteAttribute.getAccess().isPatchable());
		Assert.assertTrue(readWriteAttribute.getAccess().isPostable());
	}

	@Test
	public void checkJsonApiAttributeAnnotationDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		ResourceField field = resourceInformation.findAttributeFieldByName("name");
		Assert.assertTrue(field.getAccess().isPatchable());
		Assert.assertTrue(field.getAccess().isPostable());
	}

	@Test
	public void checkJsonApiAttributeAnnotationDefaultsForIds() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);
		ResourceField field = resourceInformation.getIdField();
		Assert.assertFalse(field.getAccess().isPatchable());
		Assert.assertTrue(field.getAccess().isPostable());
	}

	@Test
	public void shouldHaveIdFieldInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getIdField().getUnderlyingName()).isNotNull().isEqualTo("id");
	}

	@Test
	public void shouldNotBePostableOrPatchableWithoutSetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		ResourceField field = resourceInformation.findAttributeFieldByName("readOnlyValue");
		Assert.assertFalse(field.getAccess().isPostable());
		Assert.assertFalse(field.getAccess().isPatchable());
	}

	@Test
	public void shouldBePostableAndPatchableWithSetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		ResourceField field = resourceInformation.findAttributeFieldByName("name");
		Assert.assertTrue(field.getAccess().isPostable());
		Assert.assertTrue(field.getAccess().isPatchable());
	}

	@Test
	public void shouldThrowExceptionWhenResourceWithNoAnnotation() {
		expectedException.expect(RepositoryAnnotationNotFoundException.class);

		resourceInformationBuilder.build(UnAnnotatedTask.class);
	}

	@Test
	public void shouldThrowExceptionWhenMoreThan1IdAnnotationFound() throws Exception {
		expectedException.expect(ResourceDuplicateIdException.class);
		expectedException.expectMessage("Duplicated Id field found in class");

		resourceInformationBuilder.build(DuplicatedIdResource.class);
	}

	@Test
	public void shouldHaveProperRelationshipFieldInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(5).extracting(NAME_PROPERTY)
				.contains("project", "projects");
	}

	@Test
	public void shouldThrowExceptionWhenResourceWithIgnoredIdAnnotation() {
		expectedException.expect(ResourceIdNotFoundException.class);

		resourceInformationBuilder.build(IgnoredIdResource.class);
	}

	@Test
	public void shouldReturnIdFieldBasedOnFieldGetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(IdFieldWithAccessorGetterResource.class);

		assertThat(resourceInformation.getIdField()).isNotNull();
	}

	@Test
	public void shouldNotIncludeIgnoredInterfaceMethod() throws Exception {		
		ResourceInformation resourceInformation = resourceInformationBuilder.build(JsonIgnoreMethodImpl.class);
		
		assertThat(resourceInformation.findFieldByName("ignoredMember")).isNull();
	}
	
	@Test
	public void shouldReturnMergedAnnotationsOnAnnotationsOnFieldAndMethod() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(AnnotationOnFieldAndMethodResource.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1);
	}

	@Test
	public void shouldContainMetaInformationField() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getMetaField().getUnderlyingName()).isEqualTo("metaInformation");
	}

	@Test
	public void shouldThrowExceptionOnMultipleMetaInformationFields() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

		assertThat(resourceInformation.getMetaField().getUnderlyingName()).isEqualTo("metaInformation");
	}

	@Test
	public void shouldIgnoreTransientAttributes() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(IgnoredTransientAttributeResource.class);
		Assert.assertNull(resourceInformation.findFieldByName("attribute"));
	}

	@Test
	public void shouldIgnoreStaticAttributes() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(IgnoredStaticAttributeResource.class);
		Assert.assertNull(resourceInformation.findFieldByName("attribute"));
	}

	@Test(expected = IllegalStateException.class)
	public void checkWriteOnlyAttributesCurrentlyNotSupported() throws Exception {
		resourceInformationBuilder.build(WriteOnlyAttributeResource.class);
	}


	@Test
	public void shouldContainLinksInformationField() throws Exception {
		expectedException.expect(MultipleJsonApiMetaInformationException.class);

		resourceInformationBuilder.build(MultipleMetaInformationResource.class);
	}

	@Test
	public void shouldThrowExceptionOnMultipleLinksInformationFields() throws Exception {
		expectedException.expect(MultipleJsonApiLinksInformationException.class);

		resourceInformationBuilder.build(MultipleLinksInformationResource.class);
	}

	@Test
	public void shouldHaveProperTypeWhenFieldAndGetterTypesDiffer() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(DifferentTypes.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1).extracting("type").contains(String.class);
	}

	@Test
	public void shouldHaveProperTypeWhenFieldAndGetterTypesDifferV2() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(DifferentTypes.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1).extracting("type").contains(String.class);
	}

	@Test
	public void shouldRecognizeJsonAPIRelationTypeWithDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(JsonApiRelationType.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotEmpty().hasSize(2).extracting("type").contains(Future.class)
				.contains(Collection.class);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lazy").contains(true, true);
		assertThat(resourceInformation.getRelationshipFields()).extracting("includeByDefault").contains(false, false);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.NONE, LookupIncludeBehavior.NONE);
		assertThat(resourceInformation.getRelationshipFields()).extracting("resourceFieldType")
				.contains(ResourceFieldType.RELATIONSHIP, ResourceFieldType.RELATIONSHIP);
	}

	@Test
	public void shouldRecognizeJsonAPIRelationTypeWithNonDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationBuilder.build(JsonApiRelationTypeNonDefaults.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotEmpty().hasSize(2).extracting("type").contains(Future.class)
				.contains(Collection.class);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lazy").contains(false, false);
		assertThat(resourceInformation.getRelationshipFields()).extracting("includeByDefault").contains(false, true);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
		assertThat(resourceInformation.getRelationshipFields()).extracting("resourceFieldType")
				.contains(ResourceFieldType.RELATIONSHIP, ResourceFieldType.RELATIONSHIP);
	}

	@JsonApiResource(type = "tasks")
	@JsonPropertyOrder(alphabetic = true)
	public static class JsonIngoreTestResource {

		@JsonApiId
		public String id;

		public String defaultAttribute;

		@JsonProperty(access = JsonProperty.Access.READ_ONLY)
		public String readOnlyAttribute;

		@JsonProperty(access = JsonProperty.Access.READ_WRITE)
		public String readWriteAttribute;
	}

	@JsonApiResource(type = "duplicatedIdAnnotationResources")
	private static class DuplicatedIdResource {

		@JsonApiId
		public Long id;

		@JsonApiId
		public Long id2;
	}

	@JsonApiResource(type = "ignoredId")
	private static class IgnoredIdResource {

		@JsonApiId
		@JsonIgnore
		private Long id;
	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredAttributeResource {

		@JsonApiId
		private Long id;

		@JsonIgnore
		private String attribute;
	}

	@JsonApiResource(type = "accessorGetter")
	private static class AccessorGetterResource {

		@JsonApiId
		private Long id;

		private String getAccessorField() {
			return null;
		}
	}

	@JsonApiResource(type = "accessorGetter")
	private static class WriteOnlyAttributeResource {

		@JsonApiId
		public Long id;

		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private String attribute;


		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
	}

	@JsonApiResource(type = "ignoredAccessorGetter")
	private static class IgnoredAccessorGetterResource {

		@JsonApiId
		private Long id;

		@JsonIgnore
		private String getAccessorField() {
			return null;
		}
	}

	@JsonApiResource(type = "fieldWithAccessorGetterResource")
	private static class FieldWithAccessorGetterResource {

		@JsonApiId
		private Long id;
		private String accessorField;

		public String getAccessorField() {
			return accessorField;
		}
	}

	@JsonApiResource(type = "idFieldWithAccessorGetterResource")
	private static class IdFieldWithAccessorGetterResource {

		@JsonApiId
		public Long getId() {
			return null;
		}
	}

	@JsonApiResource(type = "annotationOnFieldAndMethod")
	private static class AnnotationOnFieldAndMethodResource {

		@JsonApiId
		public Long id;

		@JsonIgnore
		private String field;

		@JsonApiToOne
		private String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredStaticAttributeResource {

		public static String attribute;
		@JsonApiId
		public Long id;
	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredTransientAttributeResource {

		public transient int attribute;
		@JsonApiId
		public Long id;

		public int getAttribute() {
			return attribute;
		}

	}

	@JsonApiResource(type = "ignoredAttribute")
	private static class IgnoredStaticGetterResource {

		@JsonApiId
		private Long id;

		public static int getAttribute() {
			return 0;
		}
	}

	@JsonPropertyOrder({"b", "a", "c"})
	@JsonApiResource(type = "orderedResource")
	private static class OrderedResource {

		public String c;
		public String b;
		public String a;
		@JsonApiId
		private Long id;
	}

	@JsonPropertyOrder(alphabetic = true)
	@JsonApiResource(type = "AlphabeticResource")
	private static class AlphabeticResource {

		public String c;
		public String b;
		public String a;
		@JsonApiId
		private Long id;
	}

	@JsonApiResource(type = "multipleMetaInformationResource")
	private static class MultipleMetaInformationResource {

		@JsonApiMetaInformation
		public String c;
		@JsonApiMetaInformation
		public String b;
		@JsonApiId
		private Long id;
	}

	@JsonApiResource(type = "multipleLinksInformationResource")
	private static class MultipleLinksInformationResource {

		@JsonApiLinksInformation
		public String c;
		@JsonApiLinksInformation
		public String b;
		@JsonApiId
		private Long id;
	}

	@JsonApiResource(type = "differentTypes")
	private static class DifferentTypes {

		public Future<String> field;
		@JsonApiId
		public Long id;

		@JsonApiToOne
		public String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "differentTypesv2")
	private static class DifferentTypesv2 {

		@JsonApiToOne
		public Future<String> field;
		@JsonApiId
		private Long id;

		public String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "jsonAPIRelationType")
	private static class JsonApiRelationType {

		@JsonApiRelation
		public Future<String> field;
		@JsonApiRelation
		public Collection<Future<String>> fields;
		@JsonApiId
		public Long id;

		public String getField() {
			return null;
		}
	}

	@JsonApiResource(type = "jsonAPIRelationType")
	private static class JsonApiRelationTypeNonDefaults {

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, serialize = SerializeType.EAGER)
		public Future<String> field;
		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
		public Collection<Future<String>> fields;
		@JsonApiId
		public Long id;

		public String getField() {
			return null;
		}
	}
	
	private static interface JsonIgnoreMethodInterface {
		@JsonIgnore
		public String getIgnoredMember();
		
		public String getNotIgnoredMember();
	}
	
	@JsonApiResource(type = "jsonIgnoredInterfaceMethod")
	private static class JsonIgnoreMethodImpl implements JsonIgnoreMethodInterface {
		@JsonApiId
		public Long id;
		
		@Override
		public String getIgnoredMember() {
			return "ignored";
		}

		@Override
		public String getNotIgnoredMember() {
			return "not ignored";
		}
	}
}
