package io.crnk.meta;

import java.util.Arrays;

import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaKeyTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		Module.ModuleContext moduleContext = Mockito.mock(Module.ModuleContext.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(moduleContext.getResourceRegistry()).thenReturn(resourceRegistry);

		lookup = new MetaLookup();
		lookup.addProvider(new ResourceMetaProvider());
		lookup.setModuleContext(moduleContext);
		lookup.initialize();
	}

	@Test
	public void parse() {
		MetaJsonObject metaKeyType = lookup.getMeta(SomePrimaryKey.class, MetaJsonObject.class);

		MetaAttribute keyAttr = new MetaAttribute();
		keyAttr.setType(metaKeyType);

		MetaKey metaKey = new MetaKey();
		metaKey.setElements(Arrays.asList(keyAttr));

		SomePrimaryKey key = new SomePrimaryKey();
		key.setAttr1("test");
		key.setAttr2(13);

		String keyString = metaKey.toKeyString(key);
		Assert.assertEquals("test-13", keyString);
	}

	public static class SomePrimaryKey {

		private String attr1;

		private int attr2;

		public String getAttr1() {
			return attr1;
		}

		public void setAttr1(String attr1) {
			this.attr1 = attr1;
		}

		public int getAttr2() {
			return attr2;
		}

		public void setAttr2(int attr2) {
			this.attr2 = attr2;
		}
	}
}
