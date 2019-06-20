package io.crnk.ui.presentation.factory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.PresentationType;
import io.crnk.ui.presentation.element.FormContainerElement;
import io.crnk.ui.presentation.element.FormElement;
import io.crnk.ui.presentation.element.FormElements;
import io.crnk.ui.presentation.element.PresentationElement;

public class DefaultFormFactory implements PresentationElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		return env.getAcceptedTypes().contains(PresentationType.FORM);
	}

	@Override
	public FormContainerElement create(PresentationEnvironment env) {
		MetaResource resource = (MetaResource) env.getElement();

		FormContainerElement form = new FormContainerElement();
		for (MetaAttribute attribute : resource.getAttributes()) {
			buildElement(env, form.getElements(), new ArrayDeque(Arrays.asList(attribute)));
		}
		return form;
	}

	private void buildElement(PresentationEnvironment env, FormElements elements, ArrayDeque<MetaAttribute> attributePath) {
		MetaAttribute lastAttribute = attributePath.getLast();
		MetaType type = lastAttribute.getType();
		if (isIgnored(attributePath)) {
			return;
		}

		String label = PresentationBuilderUtils.getLabel(attributePath);

		if (type instanceof MetaDataObject && !lastAttribute.isAssociation()) {
			for (MetaAttribute nestedAttribute : type.asDataObject().getAttributes()) {
				if (!attributePath.contains(nestedAttribute)) {
					ArrayDeque nestedPath = new ArrayDeque();
					nestedPath.addAll(attributePath);
					nestedPath.add(nestedAttribute);
					buildElement(env, elements, nestedPath);
				}
			}
		}
		else {
			PresentationEnvironment elementEnv = env.clone();
			elementEnv.setAttributePath(attributePath);
			elementEnv.setAcceptedTypes(Arrays.asList(PresentationType.FORM_ELEMENT, PresentationType.DISPLAY));
			elementEnv.setType(type);
			PresentationElement element = env.createElement(elementEnv);

			PathSpec pathSpec = PathSpec.of(attributePath.stream().map(it -> it.getName()).collect(Collectors.toList()));

			//String valuePath =  PresentationBuilderUtils.getValuePath(attributePath);
			String id = pathSpec.toString(); //valuePath.join(valuePath, '.');

			FormElement formElement = new FormElement();
			formElement.setId(id);
			formElement.setLabel(label);
			formElement.setAttributePath(pathSpec);
			formElement.setEditable(env.isEditable());
			formElement.setComponent(element);
			//column.setEditComponent();
			// column.setFilter
			// column.setWidth
			// column.setTyleClass
			elements.add(formElement);
		}
	}

	private boolean isIgnored(ArrayDeque<MetaAttribute> attributePath) {
		return attributePath.getLast().isVersion();
	}

}
