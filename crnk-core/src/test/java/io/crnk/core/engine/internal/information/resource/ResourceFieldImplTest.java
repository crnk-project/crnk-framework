package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourceFieldImplTest {

	@Test
	public void checkToString() {
		ResourceFieldImpl impl = new ResourceFieldImpl("test-name", "testName", ResourceFieldType.ID, String.class, String
				.class, null);

		ResourceInformation resourceInformation = Mockito.mock(ResourceInformation.class);
		Mockito.when(resourceInformation.getResourceType()).thenReturn("someResource");
		Mockito.when(resourceInformation.getResourceClass()).thenReturn((Class) Resource.class);
		impl.setResourceInformation(resourceInformation);

		Assert.assertEquals("ResourceFieldImpl[resourceClass=class io.crnk.core.engine.document.Resource, jsonName=test-name,resourceType=someResource]", impl.toString());
	}

}
