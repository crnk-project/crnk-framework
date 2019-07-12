package io.crnk.ui.presentation.factory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.PresentationType;
import io.crnk.ui.presentation.annotation.PresentationFullTextSearchable;
import io.crnk.ui.presentation.element.ActionElement;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.QueryElement;
import io.crnk.ui.presentation.element.TableColumnElement;

public class DefaultExplorerFactory implements PresentationElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		// accept as root explorer or for nested many relationships
		MetaType type = env.getType();
		ArrayDeque<MetaAttribute> attributePath = env.getAttributePath();
		return env.getAcceptedTypes().contains(PresentationType.EXPLORER) ||
				attributePath != null && attributePath.getLast().isAssociation()
						&& type != null && type.isCollection();
	}

	@Override
	public ExplorerElement create(PresentationEnvironment env) {
		MetaResource rootResource = (MetaResource) env.getElement();
		MetaResource nestedResource = rootResource;

		ArrayDeque<MetaAttribute> attributePath = env.getAttributePath();
		PreconditionUtil.verify(attributePath == null || attributePath.size() < 2, "only relationships supported");
		MetaAttribute relationshipAttribute = attributePath != null ? attributePath.getFirst() : null;
		if (relationshipAttribute != null) {
			nestedResource = (MetaResource) relationshipAttribute.getType().getElementType();
		}

		PresentationService service = env.getService();

		DataTableElement table = createTable(env, nestedResource);

		ExplorerElement explorerElement = new ExplorerElement();
		explorerElement.setId(toId(service, nestedResource));
		explorerElement.setComponentId("explorer");
		explorerElement.setTable(table);
		explorerElement.addAction(createRefreshAction());
		if (rootResource != nestedResource) {
			explorerElement.setPath(StringUtils.nullToEmpty(service.getPath()) + rootResource.getResourcePath() + "/" + relationshipAttribute.getName());

			// no need to show parent again when nested
			MetaAttribute oppositeAttribute = relationshipAttribute.getOppositeAttribute();
			if (oppositeAttribute != null) {
				TableColumnElement oppositeColumn = table.getColumns().getElements().get(oppositeAttribute.getName());
				if (oppositeColumn != null) {
					table.getColumns().remove(oppositeColumn);
				}
			}
		}
		else {
			explorerElement.setPath(service.getPath() + rootResource.getResourcePath());
		}
		explorerElement.setServiceName(service.getServiceName());
		explorerElement.setServicePath(service.getPath());
		explorerElement.setFullTextSearchPaths(getSearchPaths(nestedResource));

		if (nestedResource.isInsertable()) {
			explorerElement.addAction(createPostAction());
		}

		QueryElement query = explorerElement.getBaseQuery();
		query.setResourceType(nestedResource.getResourceType());
		query.setInclusions(PresentationBuilderUtils.computeIncludes(explorerElement.getTable().getChildren()));
		return explorerElement;
	}

	private String toId(PresentationService service, MetaResource resource) {
		return service.getServiceName() + "-" + resource.getId();
	}


	private DataTableElement createTable(PresentationEnvironment env, MetaResource tableType) {
		PresentationEnvironment tableEnv = env.clone().setAcceptedTypes(Collections.singletonList(PresentationType.TABLE));
		tableEnv.setType(tableType);
		tableEnv.setElement(tableType);
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
