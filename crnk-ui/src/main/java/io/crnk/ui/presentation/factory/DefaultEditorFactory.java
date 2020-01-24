package io.crnk.ui.presentation.factory;

import java.util.Collections;

import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.PresentationType;
import io.crnk.ui.presentation.element.ActionElement;
import io.crnk.ui.presentation.element.EditorElement;
import io.crnk.ui.presentation.element.FormContainerElement;
import io.crnk.ui.presentation.element.QueryElement;

public class DefaultEditorFactory implements PresentationElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		return env.getAcceptedTypes().contains(PresentationType.EDITOR);
	}

	@Override
	public EditorElement create(PresentationEnvironment env) {
		MetaResource resource = (MetaResource) env.getElement();
		PresentationService service = env.getService();

		EditorElement editorElement = new EditorElement();
		editorElement.setId(toId(service, resource));
		editorElement.setForm(createForm(env));
		editorElement.setPath(service.getPath() + resource.getResourcePath());
		editorElement.setServiceName(service.getServiceName());
		editorElement.setServicePath(service.getPath());
		editorElement.setComponentId("editor");

		QueryElement query = editorElement.getBaseQuery();
		query.setResourceType(resource.getResourceType());
		query.setInclusions(PresentationBuilderUtils.computeIncludes(editorElement.getForm().getChildren()));
		return editorElement;
	}


	public static String toId(PresentationService service, MetaResource resource) {
		return service.getServiceName() + "-" + resource.getResourceType();
	}


	private FormContainerElement createForm(PresentationEnvironment env) {
		PresentationEnvironment formEnv = env.clone().setAcceptedTypes(Collections.singletonList(PresentationType.FORM));
		return (FormContainerElement) env.createElement(formEnv);

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
}
