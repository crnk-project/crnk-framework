package io.crnk.core.engine.internal.dispatcher;

import io.crnk.core.engine.internal.dispatcher.controller.BaseController;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;

import java.util.Set;

/**
 * Gets the instances of the {@link BaseController}'s.
 */
public interface ControllerLookup {

	/**
	 * @return the instances of the {@link BaseController}'s.
	 */
	Set<Controller> getControllers();
}
