package io.crnk.core.queryspec.mapper;

import java.lang.reflect.Type;
import java.util.List;

public class QueryPathSpec {

	private Type valueType;

	private List<String> attributePath;

	public QueryPathSpec(Type valueType, List<String> attributePath) {
		this.valueType = valueType;
		this.attributePath = attributePath;
	}

	public Type getValueType() {
		return valueType;
	}

	public List<String> getAttributePath() {
		return attributePath;
	}
}
