package io.crnk.core.module;

import java.util.List;

public interface ModuleExtensionAware<E extends ModuleExtension> extends InitializingModule {

	/**
	 * Extensions given to this module by other modules. The method is called
	 * before {@link InitializingModule#init()}.
	 *
	 * @param extensions
	 */
	void setExtensions(List<E> extensions);
}
