package io.crnk.core.queryspec.mapper;

import java.util.List;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.queryspec.FilterOperator;

public class QueryParameter {


	private String name;

	private QueryParameterType type;

	private String pagingType;

	private ResourceInformation resourceInformation;

	private FilterOperator operator;

	private List<String> attributePath;

	private Set<String> values;

	private TypeParser typeParser;

	public QueryParameter(TypeParser typeParser) {
		this.typeParser = typeParser;
	}

	public <T> T getValue(Class<T> type) {
		if (values.size() != 1) {
			throw new ParametersDeserializationException("expected a Long for " + toString());
		}
		try {
			String strValue = values.iterator().next();
			return typeParser.parse(strValue, type);
		}
		catch (NumberFormatException e) {
			throw new ParametersDeserializationException("expected a Long for " + toString());
		}
	}

	@Override
	public String toString() {
		return name + "=" + values;
	}


	public String getName() {
		return name;
	}

	public QueryParameterType getType() {
		return type;
	}

	public ResourceInformation getResourceInformation() {
		return resourceInformation;
	}

	public FilterOperator getOperator() {
		return operator;
	}

	public List<String> getAttributePath() {
		return attributePath;
	}

	public Set<String> getValues() {
		return values;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(QueryParameterType type) {
		this.type = type;
	}

	public void setValues(Set<String> values) {
		this.values = values;
	}

	public void setOperator(FilterOperator operator) {
		this.operator = operator;
	}

	public void setResourceInformation(ResourceInformation resourceInformation) {
		this.resourceInformation = resourceInformation;
	}

	public String getPagingType() {
		return pagingType;
	}

	public void setPagingType(String pageParameter) {
		this.pagingType = pageParameter;
	}

	public void setAttributePath(List<String> attributePath) {
		this.attributePath = attributePath;
	}
}