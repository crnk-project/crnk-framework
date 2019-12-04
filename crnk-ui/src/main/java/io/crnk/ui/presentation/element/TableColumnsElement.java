package io.crnk.ui.presentation.element;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableColumnsElement extends PresentationElement {

	private List<String> elementIds = new ArrayList<>();

	private Map<String, TableColumnElement> elements = new HashMap<>();

	private boolean resizable;

	private boolean resizeMode;

	public void add(TableColumnElement column) {
		elementIds.add(column.getId());
		elements.put(column.getId(), column);
	}

	public List<String> getElementIds() {
		return elementIds;
	}

	public void setElementIds(List<String> elementIds) {
		this.elementIds = elementIds;
	}

	public Map<String, TableColumnElement> getElements() {
		return elements;
	}

	public void setElements(Map<String, TableColumnElement> elements) {
		this.elements = elements;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public boolean isResizeMode() {
		return resizeMode;
	}

	public void setResizeMode(boolean resizeMode) {
		this.resizeMode = resizeMode;
	}

	public void remove(TableColumnElement column) {
		this.elementIds.remove(column.getId());
		this.elements.remove(column.getId());
	}
}
