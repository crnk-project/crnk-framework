package io.crnk.core.engine.registry;


import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.module.TestResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;

public class HierarchicalResourceRegistryPartTest {

	@Test
	public void testRootPart() {
		HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
		part.putPart("", new DefaultResourceRegistryPart());

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		Mockito.when(information.getResourceType()).thenReturn("test");
		Mockito.when(information.getResourceClass()).thenReturn((Class) TestResource.class);
		RegistryEntry entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(information);
		RegistryEntry savedEntry = part.addEntry(entry);
		Assert.assertSame(savedEntry, entry);

		Collection<RegistryEntry> resources = part.getResources();
		Assert.assertEquals(1, resources.size());
		Assert.assertSame(entry, part.getEntry("test"));
		Assert.assertSame(entry, part.getEntry(TestResource.class));
		Assert.assertTrue(part.hasEntry("test"));
		Assert.assertTrue(part.hasEntry(TestResource.class));
	}

	@Test
	public void testChildPart() {
		HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
		part.putPart("child", new DefaultResourceRegistryPart());

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		Mockito.when(information.getResourceType()).thenReturn("child/test");
		Mockito.when(information.getResourceClass()).thenReturn((Class) TestResource.class);
		RegistryEntry entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(information);
		RegistryEntry savedEntry = part.addEntry(entry);
		Assert.assertSame(savedEntry, entry);

		Collection<RegistryEntry> resources = part.getResources();
		Assert.assertEquals(1, resources.size());
		Assert.assertSame(entry, part.getEntry("child/test"));
		Assert.assertSame(entry, part.getEntry(TestResource.class));
		Assert.assertTrue(part.hasEntry("child/test"));
		Assert.assertTrue(part.hasEntry(TestResource.class));
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingChildPart() {
		HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
		part.putPart("otherChild", new DefaultResourceRegistryPart());

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		Mockito.when(information.getResourceType()).thenReturn("child/test");
		Mockito.when(information.getResourceClass()).thenReturn((Class) TestResource.class);
		RegistryEntry entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(information);
		part.addEntry(entry);
	}
}
