package io.crnk.meta;

import java.util.Date;

import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaLookupTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		Module.ModuleContext moduleContext = Mockito.mock(Module.ModuleContext.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(moduleContext.getResourceRegistry()).thenReturn(resourceRegistry);

		lookup = new MetaLookup();
		lookup.setModuleContext(moduleContext);
		lookup.addProvider(new ResourceMetaProvider());
		lookup.initialize();
	}

	@Test
	public void testPrimitiveFloat() {
		MetaType meta = lookup.getMeta(Float.class, MetaType.class);
		Assert.assertEquals("base.float", meta.getId());
		Assert.assertEquals(Float.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(float.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveDouble() {
		MetaType meta = lookup.getMeta(Double.class, MetaType.class);
		Assert.assertEquals("base.double", meta.getId());
		Assert.assertEquals(Double.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(double.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveByte() {
		MetaType meta = lookup.getMeta(Byte.class, MetaType.class);
		Assert.assertEquals("base.byte", meta.getId());
		Assert.assertEquals(Byte.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(byte.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveInteger() {
		MetaType meta = lookup.getMeta(Integer.class, MetaType.class);
		Assert.assertEquals("base.integer", meta.getId());
		Assert.assertEquals(Integer.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(int.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveShort() {
		MetaType meta = lookup.getMeta(Short.class, MetaType.class);
		Assert.assertEquals("base.short", meta.getId());
		Assert.assertEquals(Short.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(short.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveLong() {
		MetaType meta = lookup.getMeta(Long.class, MetaType.class);
		Assert.assertEquals("base.long", meta.getId());
		Assert.assertEquals(Long.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(long.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveBoolean() {
		MetaType meta = lookup.getMeta(Boolean.class, MetaType.class);
		Assert.assertEquals("base.boolean", meta.getId());
		Assert.assertEquals(Boolean.class, meta.getImplementationClass());

		MetaType primitiveMeta = lookup.getMeta(boolean.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveString() {
		MetaType meta = lookup.getMeta(String.class, MetaType.class);
		Assert.assertEquals("base.string", meta.getId());
		Assert.assertEquals(String.class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveDate() {
		MetaType meta = lookup.getMeta(Date.class, MetaType.class);
		Assert.assertEquals("base.date", meta.getId());
		Assert.assertEquals(Date.class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveObject() {
		MetaType meta = lookup.getMeta(Object.class, MetaType.class);
		Assert.assertEquals("base.object", meta.getId());
		Assert.assertEquals(Object.class, meta.getImplementationClass());
	}
}
