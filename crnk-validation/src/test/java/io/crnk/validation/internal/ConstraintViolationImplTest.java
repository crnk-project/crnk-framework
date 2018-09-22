package io.crnk.validation.internal;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.validation.ValidationModule;
import io.crnk.validation.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Iterator;

public class ConstraintViolationImplTest {


	private ErrorData errorData;

	private CrnkBoot boot;

	private ConstraintViolationImpl violation;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(ValidationModule.create());
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.validation.mock.repository"));
		boot.boot();

		errorData =
				Mockito.spy(new ErrorDataBuilder().setDetail("testMessage").addMetaField(ConstraintViolationExceptionMapper
								.META_RESOURCE_TYPE,
						"tasks")
						.setSourcePointer("name").build());

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();

		violation = ConstraintViolationImpl.fromError(resourceRegistry, errorData);
	}

	@Test
	public void testDetailMappedToMessage() {
		Assert.assertEquals(errorData.getDetail(), violation.getMessage());
		Assert.assertNotNull(errorData.getDetail());
	}

	@Test
	public void path() {
		Path path = violation.getPropertyPath();
		Assert.assertEquals("name", path.toString());
		Assert.assertEquals(path.toString().hashCode(), path.hashCode());

		Assert.assertNotEquals(path, null);
		Assert.assertNotEquals(path, "not a path");
		Assert.assertEquals(path, path);

		Iterator<Path.Node> iterator = path.iterator();
		Assert.assertTrue(iterator.hasNext());
		Path.Node node = iterator.next();
		Assert.assertEquals("name", node.getName());
		Assert.assertEquals("name", node.toString());
		Assert.assertEquals(null, node.getKey());
		Assert.assertEquals(ElementKind.PROPERTY, node.getKind());
		try {
			node.isInIterable();
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			//
		}
		try {
			node.as((Class) String.class);
			Assert.fail();
		} catch (UnsupportedOperationException e) {
			//
		}
		Assert.assertFalse(iterator.hasNext());
	}

	@Test
	public void getRootBeanClass() {
		Assert.assertEquals(Task.class, violation.getRootBeanClass());
	}


	@Test(expected = UnsupportedOperationException.class)
	public void getRootBean() {
		violation.getRootBean();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getLeafBean() {
		violation.getLeafBean();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getInvalidValue() {
		violation.getInvalidValue();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getExecutableParameters() {
		violation.getExecutableParameters();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getExecutableReturnValue() {
		violation.getExecutableReturnValue();
	}

	@Test
	public void unwrap() {
		Assert.assertNull(violation.unwrap(String.class));
	}

	@Test
	public void getMessage() {
		violation.getMessage();
		Mockito.verify(errorData, Mockito.times(1)).getDetail();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getConstraintDescriptor() {
		violation.getConstraintDescriptor();
	}

}
