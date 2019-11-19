package io.crnk.ui.presentation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.utils.Prioritizable;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.element.EditorElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.MenuElement;
import io.crnk.ui.presentation.element.MenuElements;
import io.crnk.ui.presentation.element.PresentationElement;
import io.crnk.ui.presentation.factory.DefaultEditorFactory;
import io.crnk.ui.presentation.factory.DefaultExplorerFactory;
import io.crnk.ui.presentation.factory.DefaultFormFactory;
import io.crnk.ui.presentation.factory.DefaultLabelElementFactory;
import io.crnk.ui.presentation.factory.DefaultPlainTextElementFactory;
import io.crnk.ui.presentation.factory.DefaultTableElementFactory;
import io.crnk.ui.presentation.factory.PresentationElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresentationManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(PresentationManager.class);

	private final HttpRequestContextProvider requestContextProvider;

	private List<PresentationElementFactory> factories = new ArrayList<>();

	private Supplier<List<PresentationService>> services;

	public PresentationManager(Supplier<List<PresentationService>> services, HttpRequestContextProvider requestContextProvider) {
		this.services = services;

		this.requestContextProvider = requestContextProvider;

		this.factories.add(new DefaultTableElementFactory());
		this.factories.add(new DefaultExplorerFactory());
		this.factories.add(new DefaultEditorFactory());
		this.factories.add(new DefaultFormFactory());
		this.factories.add(new DefaultLabelElementFactory());
		this.factories.add(new DefaultPlainTextElementFactory());
	}

	public void registerFactory(PresentationElementFactory factory) {
		factories.add(factory);
		factories = Prioritizable.prioritze(factories);
	}

	public ExplorerElement getExplorer(String id) {
		ResourceRef ref = findResourceRef(id);
		return (ExplorerElement) createViewer(ref.service, ref.resource, PresentationType.EXPLORER);
	}


	public EditorElement getEditor(String id) {
		ResourceRef ref = findResourceRef(id);
		return (EditorElement) createViewer(ref.service, ref.resource, PresentationType.EDITOR);
	}


	class ResourceRef {

		PresentationService service;

		MetaResource resource;
	}

	private ResourceRef findResourceRef(String id) {
		int index = id.lastIndexOf("-");
		String serviceName = id.substring(0, index);
		String resourceMetaId = "resources." + id.substring(index + 1);

		Optional<PresentationService> optService = services.get().stream().filter(it -> it.getServiceName().equals(serviceName)).findFirst();
		if (!optService.isPresent()) {
			throw new ResourceNotFoundException("no presentation service found with name " + serviceName);
		}
		PresentationService service = optService.get();
		MetaLookup lookup = service.getLookup();
		MetaResource resource = lookup.findElement(MetaResource.class, resourceMetaId);

		int requestVersion = getRequestVersion();
		if (!resource.getVersionRange().contains(requestVersion)) {
			throw new ResourceNotFoundException("no presentation service found with name " + serviceName + " serving version " + requestVersion);
		}

		ResourceRef ref = new ResourceRef();
		ref.service = service;
		ref.resource = resource;
		return ref;
	}


	private PresentationElement createViewer(PresentationService service, MetaResource resource, PresentationType type) {
		prepareResource(resource);

		PresentationEnvironment env = createEnv(service, resource);
		env.setAcceptedTypes(Arrays.asList(type));
		return createElement(env);
	}

	private void prepareResource(MetaResource resource) {
		for (MetaAttribute attribute : resource.getAttributes()) {
			if (attribute.getParent() == null) {
				attribute.setParent(resource);
			}
		}
	}

	private PresentationEnvironment createEnv(PresentationService service, MetaResource resource) {
		PresentationEnvironment env = new PresentationEnvironment();
		env.setEditable(false);
		env.setElement(resource);
		env.setType(resource);
		env.setService(service);
		env.setManager(this);
		env.setRequestVersion(getRequestVersion());
		return env;
	}

	public Map<String, EditorElement> getEditors() {
		return (Map) getViewers(PresentationType.EDITOR);
	}

	public Map<String, ExplorerElement> getExplorers() {
		return (Map) getViewers(PresentationType.EXPLORER);
	}

	public List<PresentationService> getServices() {
		return services.get();
	}

	public Map<String, PresentationElement> getViewers(PresentationType type) {
		HashMap<String, PresentationElement> map = new HashMap<>();
		for (PresentationService service : services.get()) {
			try {
				MetaLookup lookup = service.getLookup();
				List<MetaResource> resources = lookup.findElements(MetaResource.class);
				for (MetaResource resource : resources) {
					if (!isIgnored(resource)) {
						PresentationElement element = createViewer(service, resource, type);
						map.put(element.getId(), element);
					}
				}
			} catch (Exception e) {
				LOGGER.error("failed to retrieve meta data from " + service, e);
			}
		}
		return map;
	}

	private boolean isIgnored(MetaResource resource) {
		int requestVersion = getRequestVersion();
		return resource.getResourceType().startsWith("meta/") || resource.getRepository() == null ||
				!resource.getRepository().isExposed() || !resource.getVersionRange().contains(requestVersion);
	}

	private int getRequestVersion() {
		HttpRequestContext requestContext = requestContextProvider.getRequestContext();
		return requestContext.getQueryContext().getRequestVersion();
	}

	private MenuElements createMenu() {
		MenuElements menuElements = new MenuElements();
		List<MetaResource> resources = new ArrayList<>();// getMetaLookup().findElements(MetaResource.class);
		for (MetaResource resource : resources) {

			MenuElement menuElement = new MenuElement();
			menuElement.setLabel(resource.getName());
			menuElement.setId(resource.getResourceType());
			menuElements.addElement(menuElement);
		}
		return menuElements;
	}


	public PresentationElement createElement(PresentationEnvironment env) {
		for (PresentationElementFactory factory : factories) {
			if (factory.accepts(env)) {
				return factory.create(env);
			}
		}

		throw new IllegalStateException("failed to setup " + env);
	}
}
