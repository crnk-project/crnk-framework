package io.crnk.core.engine.internal.dispatcher;

import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.exception.MethodNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores a list of controllers which are used to process the incoming requests.
 *
 * @see HttpRequestDispatcherImpl
 */
public class ControllerRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerRegistry.class);

	private final List<Controller> controllers = new LinkedList<>();

	public ControllerRegistry(Collection<Controller> controllers) {
		if (controllers != null) {
			this.controllers.addAll(controllers);
		}
	}

	/**
	 * Adds Crnk controller to the registry. Should be called at initialization time.
	 *
	 * @param controller a controller to be added
	 */
	public void addController(Controller controller) {
		controllers.add(controller);
	}

	/**
	 * Iterate over all registered controllers to get the first suitable one.
	 *
	 * @param jsonPath    built JsonPath object mad from request path
	 * @param requestType type of a HTTP request
	 * @return suitable controller
	 */
	public Controller getController(JsonPath jsonPath, String requestType) {
		for (Controller controller : controllers) {
			if (controller.isAcceptable(jsonPath, requestType)) {
				LOGGER.debug("using controller {}", controller);
				return controller;
			}
		}
		throw new MethodNotFoundException(PathBuilder.build(jsonPath), requestType);
	}

	public List<Controller> getControllers() {
		return controllers;
	}
}
