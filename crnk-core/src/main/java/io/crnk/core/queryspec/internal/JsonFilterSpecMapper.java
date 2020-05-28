package io.crnk.core.queryspec.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.mapper.QueryPathResolver;
import io.crnk.core.queryspec.mapper.QueryPathSpec;
import io.crnk.core.queryspec.mapper.QuerySpecUrlContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonFilterSpecMapper {

	private final QuerySpecUrlContext context;

	private final Map<String, FilterOperator> supportedOperators;

	private final QueryPathResolver pathResolver;

	private final FilterOperator defaultOperator;

	public JsonFilterSpecMapper(QuerySpecUrlContext ctx, Map<String, FilterOperator> supportedOperators, FilterOperator defaultOperator, QueryPathResolver pathResolver) {
		this.context = ctx;
		this.defaultOperator = defaultOperator;
		this.supportedOperators = supportedOperators;
		this.pathResolver = pathResolver;
	}

	public List<FilterSpec> deserialize(JsonNode jsonNode, ResourceInformation resourceInformation, QueryContext queryContext) {
		// we support both the serialized FilterSpec (crnk-specific) and a more compact, user-friendly format
		if (isSerializedFilterSpec(jsonNode)) {
			ObjectMapper objectMapper = context.getObjectMapper();
			try {

				ObjectReader pathReader = objectMapper.readerFor(PathSpec.class);
				ArrayNode arrayNode = (ArrayNode) jsonNode;
				List<FilterSpec> filterSpecs = new ArrayList<>();
				for (int i = 0; i < arrayNode.size(); i++) {
					JsonNode filterNode = arrayNode.get(i);

					JsonNode pathNode = filterNode.get("path");
					JsonNode opNode = filterNode.get("operator");
					JsonNode valueNode = filterNode.get("value");
					JsonNode expressionNode = filterNode.get("expression");

					FilterOperator operator = null;
					if (opNode != null && !opNode.isNull()) {
						operator = supportedOperators.get(opNode.asText());
						if (operator == null) {
							throw new BadRequestException("unknown operator " + opNode.asText());
						}
					}else{
						operator = defaultOperator;
					}
					PathSpec pathSpec = pathNode != null && !pathNode.isNull() ? pathReader.readValue(pathNode) : null;
					Object value = valueNode != null && !valueNode.isNull() ? deserializeJsonFilterValue(resourceInformation, pathSpec, valueNode, queryContext) : null;
					List<FilterSpec> expressions = expressionNode != null && !expressionNode.isNull() ? deserialize(expressionNode, resourceInformation, queryContext) : null;
					filterSpecs.add(expressions != null ? new FilterSpec(operator, expressions) : new FilterSpec(pathSpec, operator, value));
				}
				return filterSpecs;
			}
			catch (IOException e) {
				throw new BadRequestException("failed to parse parameter", e);
			}
		}
		return deserialize(jsonNode, resourceInformation, PathSpec.empty(), queryContext);
	}

	private boolean isSerializedFilterSpec(JsonNode jsonNode) {
		if (jsonNode.isArray() && jsonNode.size() > 0) {
			return isSerializedFilterSpec(jsonNode.get(0));
		}
		if (jsonNode instanceof ObjectNode) {
			return jsonNode.has("path") && jsonNode.has("value") || jsonNode.has("expression") && jsonNode.has("operator");
		}
		return false;
	}

	public boolean isJson(String value) {
		String trimmedValue = value.trim();
		return trimmedValue.startsWith("{") && trimmedValue.endsWith("}") || trimmedValue.startsWith("[") && trimmedValue.endsWith("]");
	}

	public boolean isNested(List<FilterSpec> filterSpecs) {
		return filterSpecs.stream().filter(it -> it.hasExpressions()).findFirst().isPresent();
	}

	public JsonNode serialize(ResourceInformation resourceInformation, List<FilterSpec> filterSpecs, QueryContext queryContext) {
		return serialize(resourceInformation, filterSpecs, null, queryContext);
	}

	private JsonNode serialize(ResourceInformation resourceInformation, List<FilterSpec> filterSpecs, FilterOperator operator, QueryContext queryContext) {
		PreconditionUtil.verify(!filterSpecs.isEmpty(), "must not be empty");

		ObjectMapper objectMapper = context.getObjectMapper();

		// Operators nesting multiple filters: serialize to JSON array
		if (operator == FilterOperator.AND || operator == FilterOperator.OR) {
			ArrayNode arrayNode = objectMapper.createArrayNode();
			filterSpecs.forEach(it -> arrayNode.add(serializeFilter(resourceInformation, it, queryContext)));
			return arrayNode;
		} else {
			ObjectNode objectNode = objectMapper.createObjectNode();
			for (FilterSpec filterSpec : filterSpecs) {
				PathSpec implPath = filterSpec.getPath();

				// resourceInformation == null => json path already resolved before (happens for nesting)
				PathSpec jsonPath = resourceInformation != null && implPath != null ?
						pathResolver.resolve(resourceInformation, implPath.getElements(), QueryPathResolver.NamingType.JAVA, null, queryContext).toPathSpec()
						: implPath;

				if (!filterSpec.hasExpressions() && jsonPath.getElements().size() > 1) {
					String firstPathElement = jsonPath.getElements().get(0);
					FilterSpec nestedFilterSpec = filterSpec.clone();
					nestedFilterSpec.setPath(PathSpec.of(jsonPath.getElements().subList(1, jsonPath.getElements().size())));
					objectNode.set(firstPathElement, serializeFilter(null, nestedFilterSpec, queryContext));
				}
				else if (filterSpec.getOperator() == FilterOperator.EQ) {
					JsonNode jsonNode = serializeValue(filterSpec.getValue());
					objectNode.set(jsonPath.toString(), jsonNode);
				}
				else if (filterSpec.hasExpressions()) {
					objectNode.set(filterSpec.getOperator().toString(), serialize(resourceInformation, filterSpec.getExpression(), filterSpec.getOperator(), queryContext));
				}
				else {
					FilterSpec nestedFilterSpec = filterSpec.clone();
					nestedFilterSpec.setOperator(FilterOperator.EQ);
					objectNode.set(filterSpec.getOperator().toString(), serializeFilter(resourceInformation, nestedFilterSpec, queryContext));
				}
			}
			return objectNode;
		}
	}

	private JsonNode serializeFilter(ResourceInformation resourceInformation, FilterSpec filterSpec, QueryContext queryContext) {
		return serialize(resourceInformation, Arrays.asList(filterSpec), queryContext);
	}

	private JsonNode serializeValue(Object value) {
		ObjectMapper objectMapper = context.getObjectMapper();
		return objectMapper.valueToTree(value);
	}

	private List<FilterSpec> deserialize(JsonNode jsonNode, ResourceInformation resourceInformation, PathSpec jsonAttributePath, QueryContext queryContext) {
		if (jsonNode instanceof ArrayNode) {
			return deserializeJsonArrayFilter((ArrayNode) jsonNode, resourceInformation, jsonAttributePath, queryContext);
		}
		else if (jsonNode instanceof ObjectNode) {
			ObjectNode objectNode = (ObjectNode) jsonNode;

			List<FilterSpec> filterSpecs = new ArrayList<>();
			Iterator<String> fieldNames = objectNode.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				JsonNode element = objectNode.get(fieldName);
				FilterOperator operator = findOperator(fieldName);
				if (operator != null) {
					filterSpecs.add(deserializeJsonOperatorFilter(operator, element, resourceInformation, jsonAttributePath, queryContext));
				}
				else if (element instanceof ObjectNode) {
					PathSpec nestedAttrPath = jsonAttributePath.append(fieldName);
					filterSpecs.add(FilterSpec.and(deserialize(element, resourceInformation, nestedAttrPath, queryContext)));
				}
				else {
					PathSpec nestedJsonAttrPath = jsonAttributePath.append(fieldName);
					QueryPathSpec resolvedImplPath = pathResolver.resolve(resourceInformation, nestedJsonAttrPath.getElements(), QueryPathResolver.NamingType.JSON, "filter", queryContext);
					Object value = deserializeJsonFilterValue(resourceInformation, nestedJsonAttrPath, element, queryContext);
					filterSpecs.add(new FilterSpec(resolvedImplPath.getAttributePath(), FilterOperator.EQ, value));
				}
			}
			return filterSpecs;
		}
		else {
			throw newParseException(jsonNode);
		}
	}

	protected FilterOperator findOperator(String lastElement) {
		FilterOperator operator = supportedOperators.get(lastElement);
		// default case => upper case
		if (operator != null) {
			return operator;
		}
		// allow to ignore case
		for (FilterOperator op : supportedOperators.values()) {
			if (op.getName().equalsIgnoreCase(lastElement)) {
				return op;
			}
		}
		return null;
	}

	private Object deserializeJsonFilterValue(ResourceInformation resourceInformation, PathSpec attributePath, JsonNode jsonNode, QueryContext queryContext) {
		QueryPathSpec resolvedPath = pathResolver.resolve(resourceInformation, attributePath.getElements(), QueryPathResolver.NamingType.JSON, "filter", queryContext);
		resolvedPath.verifyFilterable();

		Class valueType = ClassUtils.getRawType(resolvedPath.getValueType());
		ObjectReader reader = context.getObjectMapper().readerFor(valueType);
		try {
			if (jsonNode instanceof ArrayNode) {
				List values = new ArrayList();
				for (JsonNode elementNode : jsonNode) {
					values.add(reader.readValue(elementNode));
				}
				return values;
			}
			return reader.readValue(jsonNode);
		}
		catch (IOException e) {
			throw new ParametersDeserializationException("failed to parse value " + jsonNode + " to type " + valueType);
		}
	}

	private List<FilterSpec> deserializeJsonArrayFilter(ArrayNode arrayNode, ResourceInformation resourceInformation, PathSpec attributePath, QueryContext queryContext) {
		List<FilterSpec> filterSpecs = new ArrayList<>();
		for (int i = 0; i < arrayNode.size(); i++) {
			filterSpecs.add(FilterSpec.and(deserialize(arrayNode.get(i), resourceInformation, attributePath, queryContext)));
		}
		return filterSpecs;
	}

	private FilterSpec deserializeJsonOperatorFilter(FilterOperator operator, JsonNode element, ResourceInformation resourceInformation, PathSpec attributePath, QueryContext queryContext) {
		List<FilterSpec> elementFilters = deserialize(element, resourceInformation, attributePath, queryContext);
		if (elementFilters.size() == 1) {
			FilterSpec elementFilter = elementFilters.get(0);
			if (elementFilter.getOperator() == FilterOperator.EQ) {
				if (operator == FilterOperator.NOT) {
					elementFilter.setOperator(FilterOperator.NEQ);
				}
				else {
					elementFilter.setOperator(operator);
				}
				return elementFilter;
			}
		}
		return new FilterSpec(operator, elementFilters);
	}

	private RuntimeException newParseException(JsonNode jsonNode) {
		try {
			String text = context.getObjectMapper().writer().writeValueAsString(jsonNode);
			throw new ParametersDeserializationException("failed to parse filter " + text);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}
