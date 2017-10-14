package io.crnk.meta.model;

import java.util.ArrayList;
import java.util.Arrays;

import io.crnk.meta.AbstractMetaTest;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Test;

public class MetaDataObjectTest extends AbstractMetaTest {


	@Test(expected = IllegalArgumentException.class)
	public void checkResolvePathWithNullNotAllowed() {
		MetaResource meta = resourceProvider.getMeta(Task.class, MetaResource.class);
		meta.resolvePath(null);
	}

	@Test
	public void checkResolveEmptyPath() {
		MetaResource meta = resourceProvider.getMeta(Task.class, MetaResource.class);
		Assert.assertEquals(MetaAttributePath.EMPTY_PATH, meta.resolvePath(new ArrayList<String>()));
	}


	@Test
	public void checkResolveSubtypeAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class, MetaResource.class);
		Assert.assertNotNull(meta.findAttribute("subTypeValue", true));
	}

	@Test(expected = IllegalStateException.class)
	public void checkCannotResolveSubtypeAttributeWithoutIncludingSubtypes() {
		MetaResource meta = resourceProvider.getMeta(Task.class, MetaResource.class);
		meta.findAttribute("subTypeValue", false);
	}

	@Test(expected = IllegalStateException.class)
	public void checkResolveInvalidAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class, MetaResource.class);
		Assert.assertNotNull(meta.findAttribute("doesNotExist", true));
	}

	@Test
	public void checkResolveMapPath() {
		MetaResource meta = resourceProvider.getMeta(Project.class, MetaResource.class);
		MetaAttributePath path = meta.resolvePath(Arrays.asList("data", "customData", "test"));
		Assert.assertEquals(2, path.length());
		Assert.assertEquals("data", path.getElement(0).getName());
		MetaMapAttribute mapAttr = (MetaMapAttribute) path.getElement(1);
		Assert.assertEquals("test", mapAttr.getKey());
		Assert.assertEquals("customData", mapAttr.getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkResolveInvalidPath() {
		MetaResource meta = resourceProvider.getMeta(Project.class, MetaResource.class);
		meta.resolvePath(Arrays.asList("name", "doesNotExist"));
	}
}
