package io.crnk.ui.presentation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.element.ActionElement;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.MenuElement;
import io.crnk.ui.presentation.element.MenuElements;
import io.crnk.ui.presentation.element.PresentationElement;
import io.crnk.ui.presentation.element.QueryElement;
import io.crnk.ui.presentation.factory.DefaultDisplayElementFactory;
import io.crnk.ui.presentation.factory.DefaultTableElementFactory;
import io.crnk.ui.presentation.factory.PresentationBuilderUtils;
import io.crnk.ui.presentation.factory.PresentationElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresentationManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(PresentationManager.class);

	private List<PresentationElementFactory> factories = new ArrayList<>();

	private Supplier<List<PresentationService>> services;

	public PresentationManager(Supplier<List<PresentationService>> services) {
		this.services = services;

		this.factories.add(new DefaultDisplayElementFactory());
		this.factories.add(new DefaultTableElementFactory());

	}

	public ExplorerElement getExplorer(String id) {
		int index = id.lastIndexOf("-");
		String serviceName = id.substring(0, index);
		String resourceMetaId = id.substring(index + 1);

		Optional<PresentationService> optService = services.get().stream().filter(it -> it.getServiceName().equals(serviceName)).findFirst();
		if (!optService.isPresent()) {
			throw new ResourceNotFoundException("no presentation service found with name " + serviceName);
		}
		PresentationService service = optService.get();

		MetaLookup lookup = service.getLookup();
		MetaResource resource = lookup.findElement(MetaResource.class, resourceMetaId);
		return createExplorer(service, resource);
	}

	public Map<String, ExplorerElement> getExplorers() {
		HashMap<String, ExplorerElement> map = new HashMap<>();

		for (PresentationService service : services.get()) {
			try {
				MetaLookup lookup = service.getLookup();
				List<MetaResource> resources = lookup.findElements(MetaResource.class);
				for (MetaResource resource : resources) {
					if (!isIgnored(resource)) {
						ExplorerElement explorer = createExplorer(service, resource);
						map.put(explorer.getId(), explorer);
					}
				}
			}
			catch (Exception e) {
				LOGGER.error("failed to retrieve meta data from " + service, e);
			}
		}
		return map;
	}

	private boolean isIgnored(MetaResource resource) {
		return resource.getResourceType().startsWith("meta/") && resource.getRepository() != null && resource.getRepository().isExposed();
	}

	private ExplorerElement createExplorer(PresentationService service, MetaResource resource) {
		ExplorerElement explorerElement = new ExplorerElement();
		explorerElement.setId(toId(service, resource));
		explorerElement.setTable(createTable(resource));
		explorerElement.addAction(createRefreshAction());
		explorerElement.setPath(service.getPath() + resource.getResourcePath());
		explorerElement.setServiceName(service.getServiceName());
		explorerElement.setServicePath(service.getPath());

		if (resource.isInsertable()) {
			explorerElement.addAction(createPostAction());
		}

		QueryElement query = explorerElement.getBaseQuery();
		query.setResourceType(resource.getResourceType());
		query.setInclusions(PresentationBuilderUtils.computeIncludes(explorerElement.getTable()));
		return explorerElement;
	}

	private String toId(PresentationService service, MetaResource resource) {
		return service.getServiceName() + "-" + resource.getId();
	}


	private DataTableElement createTable(MetaResource resource) {
		DefaultTableElementFactory builder = new DefaultTableElementFactory();
		PresentationEnvironment env = new PresentationEnvironment();
		env.setElement(resource);
		env.setType(resource);
		env.setEditable(false);
		env.setAcceptedTypes(Arrays.asList(PresentationType.TABLE));
		env.setFactory(this);
		return builder.create(env);
	}

	private ActionElement createRefreshAction() {
		ActionElement element = new ActionElement();
		element.setId("refresh");
		return element;
	}

	private ActionElement createPostAction() {
		ActionElement element = new ActionElement();
		element.setId("create");
		return element;
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
