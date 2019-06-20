package io.crnk.ui.presentation.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.PresentationType;
import io.crnk.ui.presentation.annotation.PresentationFullTextSearchable;
import io.crnk.ui.presentation.element.ActionElement;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.QueryElement;

public class DefaultExplorerFactory implements PresentationElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		return env.getAcceptedTypes().contains(PresentationType.EXPLORER);
	}

	@Override
	public ExplorerElement create(PresentationEnvironment env) {
		MetaResource resource = (MetaResource) env.getElement();
		PresentationService service = env.getService();

		ExplorerElement explorerElement = new ExplorerElement();
		explorerElement.setId(toId(service, resource));
		explorerElement.setTable(createTable(env));
		explorerElement.addAction(createRefreshAction());
		explorerElement.setPath(service.getPath() + resource.getResourcePath());
		explorerElement.setServiceName(service.getServiceName());
		explorerElement.setServicePath(service.getPath());
		explorerElement.setFullTextSearchPaths(getSearchPaths(resource));

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


	private DataTableElement createTable(PresentationEnvironment env) {
		PresentationEnvironment tableEnv = env.clone().setAcceptedTypes(Collections.singletonList(PresentationType.TABLE));
		return (DataTableElement) env.createElement(tableEnv);

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

	private List<PathSpec> getSearchPaths(MetaResource type) {
		List<PathSpec> list = new ArrayList<>();
		for (MetaAttribute attribute : type.getAttributes()) {
			if (attribute.getNatures().contains(PresentationFullTextSearchable.META_ELEMENT_NATURE)) {
				list.add(PathSpec.of(attribute.getName()));
			}
		}
		return list;
	}
}
