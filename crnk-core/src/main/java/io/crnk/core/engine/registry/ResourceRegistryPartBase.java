package io.crnk.core.engine.registry;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ResourceRegistryPartBase implements ResourceRegistryPart {


	private final CopyOnWriteArrayList<ResourceRegistryPartListener> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void addListener(ResourceRegistryPartListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ResourceRegistryPartListener listener) {
		listeners.remove(listener);
	}

	protected void notifyChange() {
		ResourceRegistryPartEvent event = ResourceRegistryPartEvent.create(this);
		for (ResourceRegistryPartListener listener : listeners) {
			listener.onChanged(event);
		}
	}
}
