package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class DefaultResourceRegistryPartTest {

	private DefaultResourceRegistryPart part;

	private RegistryEntry entry;

	@Before
	public void setup() {
		part = new DefaultResourceRegistryPart();

		ResourceInformation resourceInformation = Mockito.mock(ResourceInformation.class);
		Mockito.when(resourceInformation.getResourceType()).thenReturn("test");

		entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(resourceInformation);
	}

	@Test
	public void checkListenerEvent() {
		ResourceRegistryPartListener listener = Mockito.mock(ResourceRegistryPartListener.class);
		part.addListener(listener);
		part.addEntry(entry);
		ArgumentCaptor<ResourceRegistryPartEvent> eventCaptor =
				ArgumentCaptor.forClass(ResourceRegistryPartEvent.class);
		Mockito.verify(listener, Mockito.times(1)).onChanged(eventCaptor.capture());

	}

	@Test
	public void checkAddRemoveListeners() {
		ResourceRegistryPartListener listener = Mockito.mock(ResourceRegistryPartListener.class);
		part.addListener(listener);
		part.addEntry(entry);

		part.removeListener(listener);
		part.addEntry(entry);
		Mockito.verify(listener, Mockito.times(1)).onChanged(Mockito.any(ResourceRegistryPartEvent.class));

		part.addListener(listener);
		part.addEntry(entry);
		Mockito.verify(listener, Mockito.times(2)).onChanged(Mockito.any(ResourceRegistryPartEvent.class));
	}
}
