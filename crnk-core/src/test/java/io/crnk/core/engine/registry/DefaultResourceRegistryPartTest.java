package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class DefaultResourceRegistryPartTest {

	private DefaultResourceRegistryPart part;

	private RegistryEntry entry;

	private RegistryEntry entry2;

	private RegistryEntry entry3;

	@Before
	public void setup() {
		part = new DefaultResourceRegistryPart();

		ResourceInformation resourceInformation = Mockito.mock(ResourceInformation.class);
		Mockito.when(resourceInformation.getResourceType()).thenReturn("test");

		ResourceInformation resourceInformation2 = Mockito.mock(ResourceInformation.class);
		Mockito.when(resourceInformation2.getResourceType()).thenReturn("test2");

		ResourceInformation resourceInformation3 = Mockito.mock(ResourceInformation.class);
		Mockito.when(resourceInformation3.getResourceType()).thenReturn("test3");

		entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(resourceInformation);

		entry2 = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry2.getResourceInformation()).thenReturn(resourceInformation2);

		entry3 = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry3.getResourceInformation()).thenReturn(resourceInformation3);
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
		part.addEntry(entry2);
		Mockito.verify(listener, Mockito.times(1)).onChanged(Mockito.any(ResourceRegistryPartEvent.class));

		part.addListener(listener);
		part.addEntry(entry3);
		Mockito.verify(listener, Mockito.times(2)).onChanged(Mockito.any(ResourceRegistryPartEvent.class));
	}
}
