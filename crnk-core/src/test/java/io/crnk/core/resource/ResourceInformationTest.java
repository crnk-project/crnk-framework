package io.crnk.core.resource;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.mock.models.Task;
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
		sut = new ResourceInformation(typeParser, Task.class, "tasks", null, Arrays.asList(idField, valueField));
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
