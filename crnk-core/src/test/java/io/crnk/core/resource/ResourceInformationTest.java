package io.crnk.core.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.AnyResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourceInformationTest {

	private ResourceInformation sut;

	private TypeParser typeParser = new TypeParser();


	private ResourceFieldImpl valueField;

	@Before
	public void setup() {
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
		ResourceFieldAccessor valueIdAccessor = Mockito.mock(ResourceFieldAccessor.class);
		valueField = new ResourceFieldImpl("value", "value", ResourceFieldType.RELATIONSHIP, String.class,
				String.class, "projects", null, SerializeType.LAZY, LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
				new ResourceFieldAccess(true, true, true, true, true),
				"valueId", String.class, valueIdAccessor, RelationshipRepositoryBehavior.DEFAULT, null);
		sut = new ResourceInformation(typeParser, Task.class, "tasks", null, Arrays.asList(idField, valueField),
				OffsetLimitPagingSpec.class);
	}

	@Test
	public void onRelationshipFieldSearchShouldReturnExistingField() throws NoSuchFieldException {
		ResourceField result = sut.findRelationshipFieldByName("value");
		assertThat(result.getUnderlyingName()).isEqualTo("value");
	}

	@Test
	public void toIdString() {
		Assert.assertNull(sut.toIdString(null));
		Assert.assertEquals("13", sut.toIdString(13));
	}

	@Test
	public void toResourceIdentifier() {
		Resource resource = new Resource();
		resource.setId("13");
		resource.setType("tasks");

		Task task = new Task();
		task.setId(13L);

		ResourceIdentifier expected = new ResourceIdentifier("13", "tasks");
		Assert.assertSame(expected, sut.toResourceIdentifier(expected));
		Assert.assertNull(sut.toResourceIdentifier(null));
		Assert.assertEquals(expected, sut.toResourceIdentifier("13"));
		Assert.assertEquals(expected, sut.toResourceIdentifier(13L));
		Assert.assertEquals(expected, sut.toResourceIdentifier(task));
		Assert.assertEquals(expected, sut.toResourceIdentifier(resource));
		Assert.assertNotSame(resource, sut.toResourceIdentifier(resource));
	}

	@Test
	public void getFields() {
		Assert.assertEquals(2, sut.getFields().size());
	}

	@Test
	public void checkIdAccess() {
		Task task = new Task();
		sut.setId(task, 13L);
		Assert.assertEquals(13L, task.getId().longValue());
		Assert.assertEquals(13L, sut.getId(task));
	}


	@Test
	public void checkGetAccessor() {
		Assert.assertSame(valueField.getIdAccessor(), sut.getAccessor("valueId"));
		Assert.assertSame(valueField.getAccessor(), sut.getAccessor("value"));
	}

	@Test
	public void checkAnyAccessor() {
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
		sut = new ResourceInformation(typeParser, ResourceWithAny.class, "tasks", null, Arrays.asList(idField),
				OffsetLimitPagingSpec.class);

		ResourceWithAny resource = new ResourceWithAny();

		AnyResourceFieldAccessor accessor = sut.getAnyFieldAccessor();
		Assert.assertNull(accessor.getValue(resource, "test"));
		accessor.setValue(resource, "test", "testValue");
		Assert.assertEquals("testValue", accessor.getValue(resource, "test"));
		Assert.assertEquals("testValue", resource.getProperty("test"));
	}

	@Test(expected = ResourceException.class)
	public void checkGetAnyWithInvalidKey() {
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
		sut = new ResourceInformation(typeParser, ResourceWithAny.class, "tasks", null, Arrays.asList(idField),
				OffsetLimitPagingSpec.class);

		ResourceWithAny resource = new ResourceWithAny();

		AnyResourceFieldAccessor accessor = sut.getAnyFieldAccessor();
		accessor.getValue(resource, "notAllowed");
	}

	@Test(expected = ResourceException.class)
	public void checkSetAnyWithInvalidKey() {
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
		sut = new ResourceInformation(typeParser, ResourceWithAny.class, "tasks", null, Arrays.asList(idField),
				OffsetLimitPagingSpec.class);

		ResourceWithAny resource = new ResourceWithAny();

		AnyResourceFieldAccessor accessor = sut.getAnyFieldAccessor();
		accessor.setValue(resource, "notAllowed", "testValue");
	}

	@Test(expected = InvalidResourceException.class)
	public void checkAnyAccessorWithoutSetterThrowsException() {
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
		new ResourceInformation(typeParser, ResourceWithAnyWithoutSetter.class, "tasks", null, Arrays.asList(idField),
				OffsetLimitPagingSpec.class);
	}

	@JsonApiResource(type = "test")
	public static class ResourceWithAny {

		@JsonApiId
		private String id;

		private Map<String, String> properties = new HashMap<>();

		@JsonAnyGetter
		public String getProperty(String key) {
			if ("notAllowed".equals(key)) {
				throw new IllegalStateException();
			}
			return properties.get(key);
		}

		@JsonAnySetter
		public void setProperty(String key, String value) {
			if ("notAllowed".equals(key)) {
				throw new IllegalStateException();
			}
			this.properties.put(key, value);
		}
	}


	@JsonApiResource(type = "test")
	class ResourceWithAnyWithoutSetter {

		@JsonApiId
		private String id;

		@JsonAnyGetter
		public Map<String, String> getProperties() {
			return null;
		}
	}


}
