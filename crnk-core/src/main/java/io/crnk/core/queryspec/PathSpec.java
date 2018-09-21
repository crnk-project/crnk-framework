package io.crnk.core.queryspec;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.crnk.core.engine.internal.utils.StringUtils;

public class PathSpec {

	private List<String> elements;

	private PathSpec() {
	}

	public static PathSpec of(String... elements) {
		return elements != null ? of(Arrays.asList(elements)) : null;
	}

	public static PathSpec of(List<String> elements) {
		if (elements == null) {
			return null;
		}
		PathSpec pathSpec = new PathSpec();
		pathSpec.elements = elements;
		return pathSpec;
	}

	public static PathSpec of(String path) {
		return of(Arrays.asList(path.split("\\.")));
	}

	public List<String> getElements() {
		return elements;
	}

	public void setElements(List<String> elements) {
		this.elements = elements;
	}

	public String toString() {
		return StringUtils.join(".", elements);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PathSpec pathSpec = (PathSpec) o;
		return Objects.equals(elements, pathSpec.elements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elements);
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public SortSpec sort(Direction dir) {
		return new SortSpec(this, dir);
	}

	public FilterSpec filter(FilterOperator operator, Object value) {
		return new FilterSpec(this, operator, value);
	}
}
