package io.crnk.core.resource;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceInformationTest {

	private ResourceInformation sut;


	@Before
	public void setup() throws NoSuchFieldException {
		ResourceField idField = new ResourceFieldImpl("id", "id", ResourceFieldType.ID, Long.class, Long.class, null);
		ResourceField valueField = new ResourceFieldImpl("value", "value", ResourceFieldType.RELATIONSHIP, String.class,
				String.class, "projects");
		TypeParser typeParser = new TypeParser();
		sut = new ResourceInformation(typeParser, Task.class, "tasks",null, Arrays.asList(idField, valueField),
				new OffsetLimitPagingBehavior());
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
}
