package io.crnk.ui.presentation.factory;

import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaType;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.element.PlainTextElement;

public class DefaultPlainTextElementFactory implements PresentationElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		return env.getType() != null;
	}

	@Override
	public PlainTextElement create(PresentationEnvironment env) {
		MetaType type = env.getType();
		String id = type.getId();
		boolean isNumber = MetaPrimitiveType.ID_SHORT.equals(id) ||
				MetaPrimitiveType.ID_BYTE.equals(id) ||
				MetaPrimitiveType.ID_INT.equals(id) ||
				MetaPrimitiveType.ID_LONG.equals(id);
		PlainTextElement element = createElement();
		if (isNumber) {
			element.setComponentId("number");
		}
		else {
			element.setComponentId("plain");
		}
		return element;
	}

	protected PlainTextElement createElement() {
		return new PlainTextElement();
	}
}
