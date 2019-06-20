package io.crnk.ui.presentation.element;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.queryspec.PathSpec;

public class LabelElement extends PlainTextElement {

	private List<PathSpec> labelAttributes = new ArrayList<>();

	public List<PathSpec> getLabelAttributes() {
		return labelAttributes;
	}

	public void setLabelAttributes(List<PathSpec> labelAttributes) {
		this.labelAttributes = labelAttributes;
	}
}
