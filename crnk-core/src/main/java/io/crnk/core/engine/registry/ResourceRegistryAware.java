package io.crnk.core.engine.registry;

/**
 * Can be used by repositories to obtain a ResourceRegistry instance.
 */
public interface ResourceRegistryAware {

	void setResourceRegistry(ResourceRegistry resourceRegistry);
}
