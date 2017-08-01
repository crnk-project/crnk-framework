package io.crnk.client.module;

import io.crnk.core.module.Module;

/**
 * Used by CrnkClient.findModules and ServiceLoader to automatically detect client modules.
 */
public interface ClientModuleFactory {

	Module create();
}
