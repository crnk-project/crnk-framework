package io.crnk.meta;

import java.util.Date;
import java.util.UUID;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.test.mock.models.PrimitiveAttributeResource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class MetaLookupTest extends AbstractMetaTest {

	@Test
	public void testOptionalAttribute() {
		MetaResource meta = resourceProvider.getMeta(PrimitiveAttributeResource.class);
		MetaAttribute attribute = meta.getAttribute("optionalValue");
		Assert.assertEquals("resources.primitiveAttribute.optionalValue", attribute.getId());
		Assert.assertEquals(String.class, attribute.getType().getImplementationClass());
	}

	@Test
	public void testPrimitiveFloat() {
		MetaType meta = resourceProvider.getMeta(Float.class);
		Assert.assertEquals("base.float", meta.getId());
		Assert.assertEquals(Float.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(float.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveDouble() {
		MetaType meta = resourceProvider.getMeta(Double.class);
		Assert.assertEquals("base.double", meta.getId());
		Assert.assertEquals(Double.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(double.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveByte() {
		MetaType meta = resourceProvider.getMeta(Byte.class);
		Assert.assertEquals("base.byte", meta.getId());
		Assert.assertEquals(Byte.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(byte.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveInteger() {
		MetaType meta = resourceProvider.getMeta(Integer.class);
		Assert.assertEquals("base.integer", meta.getId());
		Assert.assertEquals(Integer.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(int.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveShort() {
		MetaType meta = resourceProvider.getMeta(Short.class);
		Assert.assertEquals("base.short", meta.getId());
		Assert.assertEquals(Short.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(short.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveLong() {
		MetaType meta = resourceProvider.getMeta(Long.class);
		Assert.assertEquals("base.long", meta.getId());
		Assert.assertEquals(Long.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(long.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveBoolean() {
		MetaType meta = resourceProvider.getMeta(Boolean.class);
		Assert.assertEquals("base.boolean", meta.getId());
		Assert.assertEquals(Boolean.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(boolean.class);
		Assert.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveString() {
		MetaType meta = resourceProvider.getMeta(String.class);
		Assert.assertEquals("base.string", meta.getId());
		Assert.assertEquals(String.class, meta.getImplementationClass());
	}

	@Test
	public void testStringArray() {
		MetaType meta = resourceProvider.getMeta(String[].class);
		Assert.assertEquals("base.string$array", meta.getId());
		Assert.assertEquals(String[].class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveDate() {
		MetaType meta = resourceProvider.getMeta(Date.class);
		Assert.assertEquals("base.date", meta.getId());
		Assert.assertEquals(Date.class, meta.getImplementationClass());
	}

	@Test
	@Ignore // add with Java 8
	public void testOffsetDateTime() throws ClassNotFoundException {
		if (ClassUtils.existsClass("java.time.OffsetDateTime")) {
			Class<?> offsetDateTimeClass = Class.forName("java.time.OffsetDateTime");
			MetaType meta = resourceProvider.getMeta(offsetDateTimeClass);
			Assert.assertEquals("base.offsetDateTime", meta.getId());
			Assert.assertEquals(offsetDateTimeClass, meta.getImplementationClass());
		}
	}

	@Test
	public void testPrimitiveUUID() {
		MetaType meta = resourceProvider.getMeta(UUID.class);
		Assert.assertEquals("base.uuid", meta.getId());
		Assert.assertEquals(UUID.class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveObject() {
		MetaType meta = resourceProvider.getMeta(Object.class);
		Assert.assertEquals("base.object", meta.getId());
		Assert.assertEquals(Object.class, meta.getImplementationClass());
	}
}
