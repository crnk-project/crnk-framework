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
import java.util.Set;

public class JsonFilterSpecMapper {

    private final QuerySpecUrlContext context;

    private final Set<FilterOperator> supportedOperators;

    private final QueryPathResolver pathResolver;

    public JsonFilterSpecMapper(QuerySpecUrlContext ctx, Set<FilterOperator> supportedOperators, QueryPathResolver pathResolver) {
        this.context = ctx;
        this.supportedOperators = supportedOperators;
        this.pathResolver = pathResolver;
    }

    public List<FilterSpec> deserialize(JsonNode jsonNode, ResourceInformation resourceInformation) {
        return deserialize(jsonNode, resourceInformation, PathSpec.empty());
    }

    public boolean isJson(String value) {
        return value.startsWith("{") && value.endsWith("}") || value.startsWith("[") && value.endsWith("]");
    }

    public boolean isNested(List<FilterSpec> filterSpecs) {
        return filterSpecs.stream().filter(it -> it.hasExpressions()).findFirst().isPresent();
    }

    public JsonNode serialize(List<FilterSpec> filterSpecs) {
        return serialize(filterSpecs, FilterOperator.AND);
    }

    private JsonNode serialize(List<FilterSpec> filterSpecs, FilterOperator operator) {
        PreconditionUtil.verify(!filterSpecs.isEmpty(), "must not be empty");

        ObjectMapper objectMapper = context.getObjectMapper();

        if (operator == FilterOperator.AND) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (FilterSpec filterSpec : filterSpecs) {
                PathSpec path = filterSpec.getPath();
                if (!filterSpec.hasExpressions() && path.getElements().size() > 1) {
                    String firstPathElement = path.getElements().get(0);
                    FilterSpec nestedFilterSpec = filterSpec.clone();
                    nestedFilterSpec.setPath(PathSpec.of(path.getElements().subList(1, path.getElements().size())));
                    objectNode.set(firstPathElement, serializeFilter(nestedFilterSpec));
                } else if (filterSpec.getOperator() == FilterOperator.EQ) {
                    JsonNode jsonNode = serializeValue(filterSpec.getValue());
                    objectNode.set(path.toString(), jsonNode);
                } else if (filterSpec.hasExpressions()) {
                    objectNode.set(filterSpec.getOperator().toString(), serialize(filterSpec.getExpression(), filterSpec.getOperator()));
                } else {
                    FilterSpec nestedFilterSpec = filterSpec.clone();
                    nestedFilterSpec.setOperator(FilterOperator.EQ);
                    objectNode.set(filterSpec.getOperator().toString(), serializeFilter(nestedFilterSpec));
                }
            }
            return objectNode;
        } else {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            filterSpecs.stream().forEach(it -> arrayNode.add(serializeFilter(it)));
            return arrayNode;
        }
    }

    private JsonNode serializeFilter(FilterSpec filterSpec) {
        return serialize(Arrays.asList(filterSpec));
    }

    private JsonNode serializeValue(Object value) {
        ObjectMapper objectMapper = context.getObjectMapper();
        return objectMapper.valueToTree(value);
    }

    private List<FilterSpec> deserialize(JsonNode jsonNode, ResourceInformation resourceInformation, PathSpec attributePath) {
        if (jsonNode instanceof ArrayNode) {
            return deserializeJsonArrayFilter((ArrayNode) jsonNode, resourceInformation, attributePath);
        } else if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;

            List<FilterSpec> filterSpecs = new ArrayList<>();
            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode element = objectNode.get(fieldName);
                FilterOperator operator = findOperator(fieldName);
                if (operator != null) {
                    filterSpecs.add(deserializeJsonOperatorFilter(operator, element, resourceInformation, attributePath));
                } else if (element instanceof ObjectNode) {
                    PathSpec nestedAttrPath = attributePath.append(fieldName);
                    filterSpecs.add(FilterSpec.and(deserialize(element, resourceInformation, nestedAttrPath)));
                } else {
                    PathSpec nestedAttrPath = attributePath.append(fieldName);
                    Object value = deserializeJsonFilterValue(resourceInformation, nestedAttrPath, element);
                    filterSpecs.add(new FilterSpec(nestedAttrPath, FilterOperator.EQ, value));
                }
            }
            return filterSpecs;
        } else {
            throw newParseException(jsonNode);
        }
    }

    protected FilterOperator findOperator(String lastElement) {
        for (FilterOperator op : supportedOperators) {
            if (op.getName().equalsIgnoreCase(lastElement)) {
                return op;
            }
        }
        return null;
    }

    private Object deserializeJsonFilterValue(ResourceInformation resourceInformation, PathSpec attributePath, JsonNode jsonNode) {
        QueryPathSpec resolvedPath = pathResolver.resolve(resourceInformation, attributePath.getElements(), QueryPathResolver.NamingType.JSON, "filter");

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
        } catch (IOException e) {
            throw new ParametersDeserializationException("failed to parse value " + jsonNode + " to type " + valueType);
        }
    }

    private List<FilterSpec> deserializeJsonArrayFilter(ArrayNode arrayNode, ResourceInformation resourceInformation, PathSpec attributePath) {
        List<FilterSpec> filterSpecs = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            filterSpecs.add(FilterSpec.and(deserialize(arrayNode.get(i), resourceInformation, attributePath)));
        }
        return filterSpecs;
    }

    private FilterSpec deserializeJsonOperatorFilter(FilterOperator operator, JsonNode element, ResourceInformation resourceInformation, PathSpec attributePath) {
        List<FilterSpec> elementFilters = deserialize(element, resourceInformation, attributePath);
        if (elementFilters.size() == 1) {
            FilterSpec elementFilter = elementFilters.get(0);
            if (elementFilter.getOperator() == FilterOperator.EQ) {
                if (operator == FilterOperator.NOT) {
                    elementFilter.setOperator(FilterOperator.NEQ);
                } else {
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
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
