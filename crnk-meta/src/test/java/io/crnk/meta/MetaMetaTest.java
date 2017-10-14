package io.crnk.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaMetaTest {

	private MetaLookup lookup;

	private ResourceMetaProvider resourceProvider;

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.addModule(new TestModule());

		resourceProvider = new ResourceMetaProvider();

		MetaModuleConfig moduleConfig = new MetaModuleConfig();
		moduleConfig.addMetaProvider(resourceProvider);
		MetaModule module = MetaModule.createServerModule(moduleConfig);
		boot.addModule(module);
		boot.boot();

		lookup = module.getLookup();
	}

	@Test
	public void testAttributesProperlyDeclaredAndNotInherited() {
		MetaResource elementMeta = resourceProvider.getMeta(MetaElement.class, MetaResource.class);
		MetaResource dataMeta = resourceProvider.getMeta(MetaDataObject.class, MetaResource.class);

		Assert.assertSame(elementMeta.getAttribute("id"), dataMeta.getAttribute("id"));
		Assert.assertSame(elementMeta.getPrimaryKey(), dataMeta.getPrimaryKey());
	}

	@Test
	public void testMetaElementImmutable() {
		MetaResource dataMeta = resourceProvider.getMeta(MetaDataObject.class, MetaResource.class);
		Assert.assertFalse(dataMeta.isUpdatable());
		Assert.assertFalse(dataMeta.isInsertable());
		Assert.assertFalse(dataMeta.isDeletable());
		Assert.assertNotEquals(0, dataMeta.getAttributes().size());
		for (MetaAttribute attr : dataMeta.getAttributes()) {
			Assert.assertFalse(attr.isUpdatable());
			Assert.assertFalse(attr.isInsertable());
		}
	}

	@Test
	public void testLinksNaming() {
		MetaResource taskMeta = resourceProvider.getMeta(Task.class, MetaResource.class);
		MetaAttribute linksInformation = taskMeta.getAttribute("linksInformation");
		MetaType type = linksInformation.getType();
		Assert.assertEquals(type.getId(), "resources.tasks$links");
		Assert.assertEquals(type.getName(), "TaskLinks");
	}


	@Test
	public void testMetaNaming() {
		MetaResource taskMeta = resourceProvider.getMeta(Task.class, MetaResource.class);
		MetaAttribute metaInformation = taskMeta.getAttribute("metaInformation");
		MetaType type = metaInformation.getType();
		Assert.assertEquals(type.getId(), "resources.tasks$meta");
		Assert.assertEquals(type.getName(), "TaskMeta");
	}

	@Test
	public void testNonMetaElementMutable() {
		MetaResource dataMeta = resourceProvider.getMeta(Task.class, MetaResource.class);
		Assert.assertTrue(dataMeta.isUpdatable());
		Assert.assertTrue(dataMeta.isInsertable());
		Assert.assertTrue(dataMeta.isDeletable());
		Assert.assertNotEquals(0, dataMeta.getAttributes().size());
		for (MetaAttribute attr : dataMeta.getAttributes()) {
			Assert.assertTrue(attr.isInsertable());
		}
	}

	@Test
	public void testMetaDataObjectMeta() {
		MetaResource meta = resourceProvider.getMeta(MetaDataObject.class, MetaResource.class);

		MetaAttribute elementTypeAttr = meta.getAttribute("elementType");
		Assert.assertNotNull(elementTypeAttr);
		Assert.assertNotNull(elementTypeAttr.getType());
		Assert.assertEquals("resources.meta.type.elementType", elementTypeAttr.getId());

		MetaAttribute attrsAttr = meta.getAttribute("attributes");
		Assert.assertNotNull(attrsAttr.getType());

		MetaAttribute childrenAttr = meta.getAttribute("children");
		Assert.assertEquals("resources.meta.element.children", childrenAttr.getId());
		Assert.assertEquals("resources.meta.element$list", childrenAttr.getType().getId());
	}
}
