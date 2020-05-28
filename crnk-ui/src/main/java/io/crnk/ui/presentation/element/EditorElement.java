package io.crnk.ui.presentation.element;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.data.facet.annotation.Facet;


@JsonApiResource(type = "uiEditor", resourcePath = "presentation/editor")
public class EditorElement extends PresentationElement {

	@Facet
	private String serviceName;

	private String servicePath;

	private ViewHeaderElement header = new ViewHeaderElement();

	private QueryElement baseQuery = new QueryElement();

	private ActionContainerElement actions = new ActionContainerElement();

	private FormContainerElement form = new FormContainerElement();

	private ObjectNode newResourceTemplate;

	private String path;

	public EditorElement() {
	}

	public <T extends EditorElement> T toSubClass(Class<T> subClass) {
		T element = ClassUtils.newInstance(subClass);
		element.setId(getId());
		element.setComponentId(getComponentId());
		element.setServiceName(serviceName);
		element.setServicePath(servicePath);
		element.setHeader(header);
		element.setBaseQuery(baseQuery);
		element.setActions(actions);
		element.setForm(form);
		element.setNewResourceTemplate(newResourceTemplate);
		element.setPath(path);

		return element;
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

	public ViewHeaderElement getHeader() {
		return header;
	}

	public void setHeader(ViewHeaderElement header) {
		this.header = header;
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

	public FormContainerElement getForm() {
		return form;
	}

	public void setForm(FormContainerElement form) {
		this.form = form;
	}

	public ObjectNode getNewResourceTemplate() {
		return newResourceTemplate;
	}

	public void setNewResourceTemplate(ObjectNode newResourceTemplate) {
		this.newResourceTemplate = newResourceTemplate;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
