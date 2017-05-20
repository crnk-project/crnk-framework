package io.crnk.core.module;

public interface InitializingModule extends Module {

	/**
	 * Called once Crnk is fully initialized. From this point in time, the module is, for example,
	 * allowed to access the resource registry.
	 */
	void init();

}
