package io.crnk.ui.presentation.element;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.data.facet.annotation.Facet;

@JsonApiResource(type = "uiExplorer", resourcePath = "ui/explorer")
public class ExplorerElement extends PresentationElement {

	@Facet
	private String serviceName;

	private String servicePath;

	private String path;

	private DataTableElement table = new DataTableElement();

	private QueryElement baseQuery = new QueryElement();

	private ActionContainerElement actions = new ActionContainerElement();

	private List<PathSpec> fullTextSearchPaths = new ArrayList<>();

	public List<PathSpec> getFullTextSearchPaths() {
		return fullTextSearchPaths;
	}

	public void setFullTextSearchPaths(List<PathSpec> fullTextSearchPaths) {
		this.fullTextSearchPaths = fullTextSearchPaths;
	}

	public void addAction(ActionElement action) {
		actions.getActionIds().add(action.getId());
		actions.getActions().put(action.getId(), action);
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServicePath() {
		return servicePath;
	}

	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public DataTableElement getTable() {
		return table;
	}

	public void setTable(DataTableElement table) {
		this.table = table;
	}

	public QueryElement getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(QueryElement baseQuery) {
		this.baseQuery = baseQuery;
	}

	public ActionContainerElement getActions() {
		return actions;
	}

	public void setActions(ActionContainerElement actions) {
		this.actions = actions;
	}
}
