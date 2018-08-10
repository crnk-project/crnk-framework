package io.crnk.core.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.exception.MultipleJsonApiLinksInformationException;
import io.crnk.core.exception.MultipleJsonApiMetaInformationException;
import io.crnk.core.exception.RepositoryAnnotationNotFoundException;
import io.crnk.core.exception.ResourceDuplicateIdException;
import io.crnk.core.exception.ResourceIdNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ProjectPatchStrategy;
import io.crnk.core.mock.models.ShapeResource;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.UnAnnotatedTask;
import io.crnk.core.module.TestResourceInformationProvider;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public class DefaultResourceInformationProviderTest {

	private static final String NAME_PROPERTY = "underlyingName";

	private final ResourceInformationProvider resourceInformationProvider =
			new DefaultResourceInformationProvider(new NullPropertiesProvider(),
					new OffsetLimitPagingBehavior(),
					new DefaultResourceFieldInformationProvider(),
					new JacksonResourceFieldInformationProvider());

	private final TestResourceInformationProvider testResourceInformationProvider = new TestResourceInformationProvider();

	private final ResourceInformationProviderContext context =
			new DefaultResourceInformationProviderContext(resourceInformationProvider,
					new DefaultInformationBuilder(new TypeParser()), new TypeParser(), new ObjectMapper());

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		resourceInformationProvider.init(context);
		testResourceInformationProvider.init(context);
	}

	@Test
	public void shouldHaveResourceClassInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		assertThat(resourceInformation.getResourceClass()).isNotNull().isEqualTo(Task.class);
	}

	@Test
	public void checkJsonApiAttributeAnnotation() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		ResourceField field = resourceInformation.findAttributeFieldByName("status");
		Assert.assertFalse(field.getAccess().isPatchable());
		Assert.assertFalse(field.getAccess().isPostable());
	}

	@Test
	public void shouldDiscardParametrizedTypeWithJsonIgnore() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(ShapeResource.class);

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
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		assertThat(containsFieldWithName(resourceInformation, "deleted")).isTrue();
		assertThat(containsFieldWithName(resourceInformation, "tDeleted")).isFalse();
	}

	@Test
	public void shouldHaveGetterBooleanWithIsPrefix() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		assertThat(containsFieldWithName(resourceInformation, "completed")).isTrue();
	}

	@Test
	public void shouldNotHaveIgnoredField() throws Exception {
		// GIVEN a field that has the JsonIgnore annotation, and a corresponding getter that does not
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		// THEN we should not pick up the java bean property
		assertThat(containsFieldWithName(resourceInformation, "ignoredField")).isFalse();
	}

	@Test
	public void shouldHaveOneIdFieldOfTypeLong() {
				/*
 			Task has a Long getId() field and a boolean hasId() which is ignored, only the former should have survived
 		 */
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		assertThat(resourceInformation.getIdField()).isNotNull();
		assertThat(resourceInformation.getIdField().getType()).isEqualTo(Long.class);
		assertThat(containsFieldWithName(resourceInformation, "hasId")).isFalse();
	}

	private boolean containsFieldWithName(ResourceInformation resourceInformation, String name) {
		List<ResourceField> attributeFields = resourceInformation.getAttributeFields();
		for (ResourceField field : attributeFields) {
			if (field.getUnderlyingName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void checkJsonPropertyAccessPolicy() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(JsonIgnoreTestResource.class);
		ResourceField defaultAttribute = resourceInformation.findAttributeFieldByName("defaultAttribute");
		ResourceField readOnlyAttribute = resourceInformation.findAttributeFieldByName("readOnlyAttribute");
		ResourceField readWriteAttribute = resourceInformation.findAttributeFieldByName("readWriteAttribute");
		ResourceField writeOnlyAttribute = resourceInformation.findAttributeFieldByName("writeOnlyAttribute");
		Assert.assertTrue(defaultAttribute.getAccess().isPatchable());
		Assert.assertTrue(defaultAttribute.getAccess().isPostable());
		Assert.assertFalse(readOnlyAttribute.getAccess().isPatchable());
		Assert.assertFalse(readOnlyAttribute.getAccess().isPostable());
		Assert.assertTrue(readOnlyAttribute.getAccess().isReadable());
		Assert.assertTrue(readWriteAttribute.getAccess().isPatchable());
		Assert.assertTrue(readWriteAttribute.getAccess().isPostable());
		Assert.assertTrue(readWriteAttribute.getAccess().isReadable());
		Assert.assertTrue(writeOnlyAttribute.getAccess().isPatchable());
		Assert.assertTrue(writeOnlyAttribute.getAccess().isPostable());
		Assert.assertFalse(writeOnlyAttribute.getAccess().isReadable());
	}

	@Test
	public void checkJsonApiAttributeAnnotationDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		ResourceField field = resourceInformation.findAttributeFieldByName("name");
		Assert.assertTrue(field.getAccess().isPatchable());
		Assert.assertTrue(field.getAccess().isPostable());
	}

	@Test
	public void checkJsonApiAttributeAnnotationDefaultsForIds() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);
		ResourceField field = resourceInformation.getIdField();
		Assert.assertFalse(field.getAccess().isPatchable());
		Assert.assertTrue(field.getAccess().isPostable());
		Assert.assertTrue(field.getAccess().isReadable());
	}

	@Test
	public void shouldHaveIdFieldInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		assertThat(resourceInformation.getIdField().getUnderlyingName()).isNotNull().isEqualTo("id");
	}

	@Test
	public void shouldBeReadableButNotPostableOrPatchableWithoutSetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		ResourceField field = resourceInformation.findAttributeFieldByName("readOnlyValue");
		Assert.assertTrue(field.getAccess().isReadable());
		Assert.assertFalse(field.getAccess().isPostable());
		Assert.assertFalse(field.getAccess().isPatchable());
	}

	@Test
	public void shouldBeReadableAndPostableAndPatchableWithSetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		ResourceField field = resourceInformation.findAttributeFieldByName("name");
		Assert.assertTrue(field.getAccess().isReadable());
		Assert.assertTrue(field.getAccess().isPostable());
		Assert.assertTrue(field.getAccess().isPatchable());
	}

	@Test
	public void shouldThrowExceptionWhenResourceWithNoAnnotation() {
		expectedException.expect(RepositoryAnnotationNotFoundException.class);

		resourceInformationProvider.build(UnAnnotatedTask.class);
	}

	@Test
	public void shouldThrowExceptionWhenMoreThan1IdAnnotationFound() throws Exception {
		expectedException.expect(ResourceDuplicateIdException.class);
		expectedException.expectMessage("Duplicated Id field found in class");

		resourceInformationProvider.build(DuplicatedIdResource.class);
	}

	@Test
	public void shouldHaveProperRelationshipFieldInfoForValidResource() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(5).extracting(NAME_PROPERTY)
				.contains("project", "projects");
	}

	@Test
	public void shouldThrowExceptionWhenResourceWithIgnoredIdAnnotation() {
		expectedException.expect(ResourceIdNotFoundException.class);

		resourceInformationProvider.build(IgnoredIdResource.class);
	}

	@Test
	public void shouldReturnIdFieldBasedOnFieldGetter() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(IdFieldWithAccessorGetterResource.class);

		assertThat(resourceInformation.getIdField()).isNotNull();
	}

	@Test
	public void shouldNotIncludeIgnoredInterfaceMethod() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(JsonIgnoreMethodImpl.class);

		assertThat(resourceInformation.findFieldByName("ignoredMember")).isNull();
	}

	@Test
	public void shouldReturnMergedAnnotationsOnAnnotationsOnFieldAndMethod() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(AnnotationOnFieldAndMethodResource.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(0);
	}

	@Test
	public void shouldContainMetaInformationField() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		assertThat(resourceInformation.getMetaField().getUnderlyingName()).isEqualTo("metaInformation");
	}

	@Test
	public void shouldThrowExceptionOnMultipleMetaInformationFields() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Task.class);

		assertThat(resourceInformation.getMetaField().getUnderlyingName()).isEqualTo("metaInformation");
	}

	@Test
	public void shouldIgnoreTransientAttributes() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(IgnoredTransientAttributeResource.class);
		Assert.assertNull(resourceInformation.findFieldByName("attribute"));
	}

	@Test
	public void shouldIgnoreStaticAttributes() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(IgnoredStaticAttributeResource.class);
		Assert.assertNull(resourceInformation.findFieldByName("attribute"));
	}

	@Test
	public void checkWriteOnlyAttributesCurrentlyNotSupported() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(WriteOnlyAttributeResource.class);
		Assert.assertNull(resourceInformation.findAttributeFieldByName("attribute"));
	}


	@Test
	public void shouldContainLinksInformationField() throws Exception {
		expectedException.expect(MultipleJsonApiMetaInformationException.class);

		resourceInformationProvider.build(MultipleMetaInformationResource.class);
	}

	@Test
	public void shouldThrowExceptionOnMultipleLinksInformationFields() throws Exception {
		expectedException.expect(MultipleJsonApiLinksInformationException.class);

		resourceInformationProvider.build(MultipleLinksInformationResource.class);
	}

	@Test
	public void shouldHaveProperTypeWhenFieldAndGetterTypesDiffer() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(DifferentTypes.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1).extracting("type").contains(String.class);
	}

	@Test
	public void shouldHaveNoneForDefaultLookupBehavior() throws Exception {
		ResourceInformation resourceInformation =
				resourceInformationProvider.build(JsonResourceWithDefaultLookupBehaviorRelationship.class);

		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.NONE);
	}

	@Test
	public void shouldInheritGlobalForDefaultLookupBehaviorWhenDefault() throws Exception {
		ResourceInformationProvider resourceInformationProviderWithProperty =
				getResourceInformationProviderWithProperty(CrnkProperties.INCLUDE_AUTOMATICALLY_OVERWRITE, "true");
		ResourceInformation resourceInformation =
				resourceInformationProviderWithProperty.build(JsonResourceWithDefaultLookupBehaviorRelationship.class);

		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
	}

	@Test
	public void shouldOverrideGlobalLookupBehavior() throws Exception {
		ResourceInformationProvider resourceInformationProviderWithProperty =
				getResourceInformationProviderWithProperty(CrnkProperties.INCLUDE_AUTOMATICALLY_OVERWRITE, "true");
		ResourceInformation resourceInformation =
				resourceInformationProviderWithProperty.build(JsonResourceWithOverrideLookupBehaviorRelationship.class);

		// Global says automatically always, but relationship says only when null.
		// Relationship annotation should win in this case.
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
	}

	private ResourceInformationProvider getResourceInformationProviderWithProperty(String key, String value) {
		PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
		Mockito.when(propertiesProvider.getProperty(Mockito.eq(key))).thenReturn(value);

		ResourceInformationProvider resourceInformationProvider = new DefaultResourceInformationProvider(
				propertiesProvider,
				new OffsetLimitPagingBehavior(),
				new DefaultResourceFieldInformationProvider(),
				new JacksonResourceFieldInformationProvider());
		resourceInformationProvider.init(context);

		return resourceInformationProvider;
	}

	@Test
	public void shouldHaveProperTypeWhenFieldAndGetterTypesDifferV2() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(DifferentTypes.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotNull().hasSize(1).extracting("type").contains(String.class);
	}

	@Test
	public void shouldRecognizeJsonAPIRelationTypeWithDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(JsonApiRelationType.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotEmpty().hasSize(2).extracting("type").contains(Future.class)
				.contains(Collection.class);
		assertThat(resourceInformation.getRelationshipFields()).extracting("serializeType")
				.contains(SerializeType.LAZY, SerializeType.LAZY);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.NONE, LookupIncludeBehavior.NONE);
		assertThat(resourceInformation.getRelationshipFields()).extracting("resourceFieldType")
				.contains(ResourceFieldType.RELATIONSHIP, ResourceFieldType.RELATIONSHIP);
	}

	@Test
	public void shouldRecognizeJsonAPIRelationTypeWithNonDefaults() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(JsonApiRelationTypeNonDefaults.class);

		assertThat(resourceInformation.getRelationshipFields()).isNotEmpty().hasSize(2).extracting("type").contains(Future.class)
				.contains(Collection.class);

		assertThat(resourceInformation.getRelationshipFields()).extracting("serializeType")
				.contains(SerializeType.ONLY_ID, SerializeType.EAGER);
		assertThat(resourceInformation.getRelationshipFields()).extracting("lookupIncludeBehavior")
				.contains(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS, LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
		assertThat(resourceInformation.getRelationshipFields()).extracting("resourceFieldType")
				.contains(ResourceFieldType.RELATIONSHIP, ResourceFieldType.RELATIONSHIP);
	}

	@Test
	public void shouldIgnoreCustomGetNamedMethods() {
		ResourceInformation resourceInformation = resourceInformationProvider.build(JsonApiRelationTypeCustomGetMethod.class);

		assertNull(resourceInformation.getRelationshipFields().get(0).getIdType());
	}

	@Test
	public void buildResourceInformationWithoutResourcePath(){
		ResourceInformation resourceInformation = testResourceInformationProvider.buildWithoutResourcePath(ResourcePathResource.class);
	}

	@Test
	public void checkGetSetResourcePathFromInformationProvider(){
		assertThat(resourceInformationProvider.getResourcePath(ResourcePathResource.class)).isEqualTo("/resourcePathGetterResources");
	}

	@Test
	public void checkGetNotSetResourcePathFromInformationProvider(){
		assertThat(resourceInformationProvider.getResourcePath(IdFieldWithAccessorGetterResource.class)).isEqualTo("idFieldWithAccessorGetterResource");
	}

	@Test
	public void checkGetNotSetResourcePathFromNonResourceFromInformationProvider(){
		assertThat(resourceInformationProvider.getResourcePath(String.class)).isNull();
	}

	@JsonApiResource(type = "tasks")
	@JsonPropertyOrder(alphabetic = true)
	public static class JsonIgnoreTestResource {

		@JsonApiId
		public String id;

		public String defaultAttribute;

		@JsonProperty(access = JsonProperty.Access.READ_ONLY)
		public String readOnlyAttribute;

		@JsonProperty(access = JsonProperty.Access.READ_WRITE)
		public String readWriteAttribute;

		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		public String writeOnlyAttribute;
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

	@JsonApiResource(type="resourcePathGetterResource", resourcePath = "/resourcePathGetterResources")
	private static class ResourcePathResource {

		@JsonApiId
		public Long getId() {
			return null;
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

	@JsonPropertyOrder({ "b", "a", "c" })
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

	@JsonApiResource(type = "jsonAPIRelationType")
	private static class JsonApiRelationTypeCustomGetMethod {

		@JsonApiRelation
		public List<JsonApiRelationType> fields;

		@JsonApiId
		public Long id;

		public Iterable<Long> getFieldIds() {
			return new ArrayList<>();
		}
	}

	private interface JsonIgnoreMethodInterface {

		@JsonIgnore
		String getIgnoredMember();

		String getNotIgnoredMember();
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

	@JsonApiResource(type = "jsonResourceWithDefaultLookupBehaviorRelationship")
	private static class JsonResourceWithDefaultLookupBehaviorRelationship {

		@JsonApiId
		public Long id;

		@JsonApiRelation
		public AlphabeticResource relationship;
	}

	@JsonApiResource(type = "jsonResourceWithOverrideLookupBehaviorRelationship")
	private static class JsonResourceWithOverrideLookupBehaviorRelationship {

		@JsonApiId
		public Long id;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
		public AlphabeticResource relationship;
	}

	@Test
	public void checkJsonApiDefaultPatchStrategy() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(Project.class);
		ResourceField field = resourceInformation.findAttributeFieldByName("data");
		Assert.assertEquals(PatchStrategy.MERGE, field.getPatchStrategy());
	}

	@Test
	public void checkJsonApiPatchStrategy() throws Exception {
		ResourceInformation resourceInformation = resourceInformationProvider.build(ProjectPatchStrategy.class);
		ResourceField field = resourceInformation.findAttributeFieldByName("data");
		Assert.assertEquals(PatchStrategy.SET, field.getPatchStrategy());
	}
}
