package io.crnk.core.resource.internal;

import java.util.Arrays;
import java.util.List;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperUtil;
import io.crnk.core.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DocumentMapperUtilTest extends AbstractDocumentMapperTest {

	private DocumentMapperUtil util;

	@Before
	public void setup() {
		super.setup();
		util = new DocumentMapperUtil(resourceRegistry, objectMapper);
	}

	@Test
	public void toResourceId() {
		Task task = new Task();
		task.setId(12L);
		ResourceIdentifier id = util.toResourceId(task);
		Assert.assertEquals("tasks", id.getType());
		Assert.assertEquals("12", id.getId());
	}

	@Test
	public void nullRoResourceId() {
		Assert.assertNull(util.toResourceId(null));
	}

	@Test
	public void toResourceIds() {
		Task task = new Task();
		task.setId(12L);
		List<ResourceIdentifier> ids = util.toResourceIds(Arrays.asList(task));
		ResourceIdentifier id = ids.get(0);
		Assert.assertEquals("tasks", id.getType());
		Assert.assertEquals("12", id.getId());
	}
}
