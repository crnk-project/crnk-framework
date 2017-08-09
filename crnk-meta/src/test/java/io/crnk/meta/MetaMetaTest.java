package io.crnk.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaMetaTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.addModule(new TestModule());

		MetaModuleConfig moduleConfig = new MetaModuleConfig();
		moduleConfig.addMetaProvider(new ResourceMetaProvider());
		MetaModule module = MetaModule.createServerModule(moduleConfig);
		boot.addModule(module);
		boot.boot();

		lookup = module.getLookup();
	}

	@Test
	public void testAttributesProperlyDeclaredAndNotInherited() {
		MetaResource elementMeta = lookup.getMeta(MetaElement.class, MetaResource.class);
		MetaResource dataMeta = lookup.getMeta(MetaDataObject.class, MetaResource.class);

		Assert.assertSame(elementMeta.getAttribute("id"), dataMeta.getAttribute("id"));
		Assert.assertSame(elementMeta.getPrimaryKey(), dataMeta.getPrimaryKey());
	}

	@Test
	public void testMetaDataObjectMeta() {
		MetaResource meta = lookup.getMeta(MetaDataObject.class, MetaResource.class);

		MetaAttribute elementTypeAttr = meta.getAttribute("elementType");
		Assert.assertNotNull(elementTypeAttr);
		Assert.assertNotNull(elementTypeAttr.getType());
		Assert.assertEquals("io.crnk.meta.MetaType.elementType", elementTypeAttr.getId());

		MetaAttribute attrsAttr = meta.getAttribute("attributes");
		Assert.assertNotNull(attrsAttr.getType());

		MetaAttribute childrenAttr = meta.getAttribute("children");
		Assert.assertEquals("io.crnk.meta.MetaElement.children", childrenAttr.getId());
		Assert.assertEquals("io.crnk.meta.MetaElement$List", childrenAttr.getType().getId());
	}
}
