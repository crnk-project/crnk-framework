package io.crnk.ui.presentation.factory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.annotation.PresentationLabel;
import io.crnk.ui.presentation.element.LabelElement;

public class DefaultLabelElementFactory extends DefaultPlainTextElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		MetaType type = env.getType();
		ArrayDeque<MetaAttribute> attributePath = env.getAttributePath();
		return attributePath != null && attributePath.getLast().isAssociation()
				&& type != null && !type.isCollection() && !getLabels((MetaResource) type).isEmpty();
	}

	@Override
	public LabelElement create(PresentationEnvironment env) {
		MetaResource type = (MetaResource) env.getType();
		PresentationService service = env.getService();

		LabelElement element = (LabelElement) super.create(env);
		element.setComponentId("label");
		element.setLabelAttributes(getLabels(type));
		element.setViewerId(DefaultEditorFactory.toId(service, type));
		return element;
	}

	@Override
	protected LabelElement createElement() {
		return new LabelElement();
	}

	private List<PathSpec> getLabels(MetaResource type) {
		List<PathSpec> list = new ArrayList<>();
		for (MetaAttribute attribute : type.getAttributes()) {
			if (attribute.getNatures().containsKey(PresentationLabel.META_ELEMENT_NATURE)) {
				list.add(PathSpec.of(attribute.getName()));
			}
		}
		return list;
	}
}
