package io.crnk.core.engine.registry;

public class ResourceRegistryPartEvent {

	private ResourceRegistryPart part;

	private ResourceRegistryPartEvent() {
	}

	public static ResourceRegistryPartEvent create(ResourceRegistryPart part) {
		ResourceRegistryPartEvent event = new ResourceRegistryPartEvent();
		event.part = part;
		return event;
	}

	public ResourceRegistryPart getPart() {
		return part;
	}
}
