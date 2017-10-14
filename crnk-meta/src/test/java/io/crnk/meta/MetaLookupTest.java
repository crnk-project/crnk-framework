package io.crnk.meta;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.UUID;

public class MetaLookupTest {

	private MetaLookup lookup;

	private ResourceMetaProvider resourceMetaProvider;

	@Before
	public void setup() {
		Module.ModuleContext moduleContext = Mockito.mock(Module.ModuleContext.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(moduleContext.getResourceRegistry()).thenReturn(resourceRegistry);

		resourceMetaProvider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.setModuleContext(moduleContext);
		lookup.addProvider(resourceMetaProvider);
		lookup.initialize();
	}

	@Test
	public void testPrimitiveFloat() {
		MetaType meta = resourceMetaProvider.getMeta(Float.class, MetaType.class);
		Assert.assertEquals("base.float", meta.getId());
		Assert.assertEquals(Float.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(float.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveDouble() {
		MetaType meta = resourceMetaProvider.getMeta(Double.class, MetaType.class);
		Assert.assertEquals("base.double", meta.getId());
		Assert.assertEquals(Double.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(double.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveByte() {
		MetaType meta = resourceMetaProvider.getMeta(Byte.class, MetaType.class);
		Assert.assertEquals("base.byte", meta.getId());
		Assert.assertEquals(Byte.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(byte.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveInteger() {
		MetaType meta = resourceMetaProvider.getMeta(Integer.class, MetaType.class);
		Assert.assertEquals("base.integer", meta.getId());
		Assert.assertEquals(Integer.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(int.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveShort() {
		MetaType meta = resourceMetaProvider.getMeta(Short.class, MetaType.class);
		Assert.assertEquals("base.short", meta.getId());
		Assert.assertEquals(Short.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(short.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveLong() {
		MetaType meta = resourceMetaProvider.getMeta(Long.class, MetaType.class);
		Assert.assertEquals("base.long", meta.getId());
		Assert.assertEquals(Long.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(long.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveBoolean() {
		MetaType meta = resourceMetaProvider.getMeta(Boolean.class, MetaType.class);
		Assert.assertEquals("base.boolean", meta.getId());
		Assert.assertEquals(Boolean.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceMetaProvider.getMeta(boolean.class, MetaType.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveString() {
		MetaType meta = resourceMetaProvider.getMeta(String.class, MetaType.class);
		Assert.assertEquals("base.string", meta.getId());
		Assert.assertEquals(String.class, meta.getImplementationClass());
	}

	@Test
	public void testStringArray() {
		MetaType meta = resourceMetaProvider.getMeta(String[].class, MetaType.class);
		Assert.assertEquals("base.string$array", meta.getId());
		Assert.assertEquals(String[].class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveDate() {
		MetaType meta = resourceMetaProvider.getMeta(Date.class, MetaType.class);
		Assert.assertEquals("base.date", meta.getId());
		Assert.assertEquals(Date.class, meta.getImplementationClass());
	}

	@Test
	public void testOffsetDateTime() throws ClassNotFoundException {
		if (ClassUtils.existsClass("java.time.OffsetDateTime")) {
			Class<?> offsetDateTimeClass = Class.forName("java.time.OffsetDateTime");
			MetaType meta = resourceMetaProvider.getMeta(offsetDateTimeClass, MetaType.class);
			Assert.assertEquals("base.offsetDateTime", meta.getId());
			Assert.assertEquals(offsetDateTimeClass, meta.getImplementationClass());
		}
	}

	@Test
	public void testPrimitiveUUID() {
		MetaType meta = resourceMetaProvider.getMeta(UUID.class, MetaType.class);
		Assert.assertEquals("base.uuid", meta.getId());
		Assert.assertEquals(UUID.class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveObject() {
		MetaType meta = resourceMetaProvider.getMeta(Object.class, MetaType.class);
		Assert.assertEquals("base.object", meta.getId());
		Assert.assertEquals(Object.class, meta.getImplementationClass());
	}
}
