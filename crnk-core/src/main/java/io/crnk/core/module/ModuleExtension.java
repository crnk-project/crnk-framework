package io.crnk.core.module;

/**
 * Base interface for a model extending another (target) module. The target module provides
 * the implementation of such an extension, while the source module creates an instance
 * and registers it with its ModuleContext. Extensions are handed to the target module
 * by letting it implement ModuleExtensionAware.
 * <p>
 * Over time the Crnk engine will be modudularized and its features available trough such extensions.
 */
public interface ModuleExtension {

	/**
	 * @return module to be extended.
	 */
	Class<? extends Module> getTargetModule();

	boolean isOptional();

}
