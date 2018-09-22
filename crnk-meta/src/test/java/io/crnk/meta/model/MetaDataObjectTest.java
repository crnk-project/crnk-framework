package io.crnk.meta.model;

import io.crnk.meta.AbstractMetaTest;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.types.ProjectData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class MetaDataObjectTest extends AbstractMetaTest {


	@Test(expected = IllegalArgumentException.class)
	public void checkResolvePathWithNullNotAllowed() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		meta.resolvePath(null);
	}

	@Test
	public void checkResolveEmptyPath() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		Assert.assertEquals(MetaAttributePath.EMPTY_PATH, meta.resolvePath(new ArrayList<String>()));
	}


	@Test
	public void checkResolveSubtypeAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		Assert.assertNotNull(meta.findAttribute("subTypeValue", true));
	}

	@Test(expected = IllegalStateException.class)
	public void checkCannotResolveSubtypeAttributeWithoutIncludingSubtypes() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		meta.findAttribute("subTypeValue", false);
	}

	@Test(expected = IllegalStateException.class)
	public void checkResolveInvalidAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		Assert.assertNotNull(meta.findAttribute("doesNotExist", true));
	}

	@Test
	public void checkResolveMapPath() {
		MetaResource meta = resourceProvider.getMeta(Project.class);
		MetaAttributePath path = meta.resolvePath(Arrays.asList("data", "customData", "test"));
		Assert.assertEquals(2, path.length());
		Assert.assertEquals("data", path.getElement(0).getName());
		MetaMapAttribute mapAttr = (MetaMapAttribute) path.getElement(1);
		Assert.assertEquals("test", mapAttr.getKey());
		Assert.assertEquals("customData", mapAttr.getName());
	}


	@Test
	public void checkNestedObject() {
		MetaJsonObject meta = resourceProvider.getMeta(ProjectData.class);
		Assert.assertEquals("ProjectData", meta.getName());
		Assert.assertEquals("resources.types.projectdata", meta.getId());
		Assert.assertNotNull(meta.getAttribute("data").getType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkResolveInvalidPath() {
		MetaResource meta = resourceProvider.getMeta(Project.class);
		meta.resolvePath(Arrays.asList("name", "doesNotExist"));
	}
}
