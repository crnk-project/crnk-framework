package io.crnk.core.engine.internal.dispatcher;

import io.crnk.core.engine.internal.dispatcher.controller.BaseController;

import java.util.Set;

/**
 * Gets the instances of the {@link BaseController}'s.
 */
public interface ControllerLookup {

	/**
	 * @return the instances of the {@link BaseController}'s.
	 */
	Set<BaseController> getControllers();
}
