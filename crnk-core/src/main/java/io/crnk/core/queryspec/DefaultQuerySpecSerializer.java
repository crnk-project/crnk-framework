package io.crnk.core.queryspec;

import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.resource.RestrictedQueryParamsMembers;

import java.util.*;

public class DefaultQuerySpecSerializer implements QuerySpecSerializer {

	private ResourceRegistry resourceRegistry;

	public DefaultQuerySpecSerializer(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	private static void put(Map<String, Set<String>> map, String key, String value) {
		map.put(key, new HashSet<>(Arrays.asList(value)));
	}

	private static String toKey(List<String> attributePath) {
		return "[" + StringUtils.join(".", attributePath) + "]";
	}

	private static String addResourceType(RestrictedQueryParamsMembers type, String key, String resourceType) {
		return type.toString() + "[" + resourceType + "]" + (key != null ? key : "");
	}

	private static String serializeValue(Object value) {
		return value.toString();
	}

	@Override
	public Map<String, Set<String>> serialize(QuerySpec querySpec) {
		Map<String, Set<String>> map = new HashMap<>();
		serialize(querySpec, map);
		return map;
	}

	private void serialize(QuerySpec querySpec, Map<String, Set<String>> map) {
		String resourceType = querySpec.getResourceType();
		if (resourceType == null) {
			RegistryEntry entry = resourceRegistry.getEntry(querySpec.getResourceClass());
			if (entry == null) {
				throw new RepositoryNotFoundException(querySpec.getResourceClass());
			}
			resourceType = entry.getResourceInformation().getResourceType();
		}

		serializeFilters(querySpec, resourceType, map);
		serializeSorting(querySpec, resourceType, map);
		serializeIncludedFields(querySpec, resourceType, map);
		serializeIncludedRelations(querySpec, resourceType, map);
		serializePagination(querySpec, resourceType, map);

		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
			serialize(relatedSpec, map);
		}
	}

	void serializeFilters(QuerySpec querySpec, String resourceType, Map<String, Set<String>> map) {
		for (FilterSpec filterSpec : querySpec.getFilters()) {
			if (filterSpec.hasExpressions()) {
				throw new UnsupportedOperationException("filter expressions like and and or not yet supported");
			}
			String attrKey = toKey(filterSpec.getAttributePath()) + "[" + filterSpec.getOperator().getName() + "]";
			String key = addResourceType(RestrictedQueryParamsMembers.filter, attrKey, resourceType);

			if (filterSpec.getValue() instanceof Collection) {
				Collection<?> col = filterSpec.getValue();
				Set<String> values = new HashSet<>();
				for (Object elem : col) {
					values.add(serializeValue(elem));
				}
				map.put(key, values);
			} else {
				String value = serializeValue(filterSpec.getValue());
				put(map, key, value);
			}
		}
	}

	public void serializeSorting(QuerySpec querySpec, String resourceType, Map<String, Set<String>> map) {
		if (!querySpec.getSort().isEmpty()) {
			String key = addResourceType(RestrictedQueryParamsMembers.sort, null, resourceType);

			StringBuilder builder = new StringBuilder();
			for (SortSpec filterSpec : querySpec.getSort()) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				if (filterSpec.getDirection() == Direction.DESC) {
					builder.append("-");
				}
				builder.append(StringUtils.join(".", filterSpec.getAttributePath()));
			}
			put(map, key, builder.toString());
		}
	}

	void serializeIncludedFields(QuerySpec querySpec, String resourceType, Map<String, Set<String>> map) {
		if (!querySpec.getIncludedFields().isEmpty()) {
			String key = addResourceType(RestrictedQueryParamsMembers.fields, null, resourceType);

			StringBuilder builder = new StringBuilder();
			for (IncludeFieldSpec includedField : querySpec.getIncludedFields()) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(StringUtils.join(".", includedField.getAttributePath()));
			}
			put(map, key, builder.toString());
		}
	}

	void serializeIncludedRelations(QuerySpec querySpec, String resourceType, Map<String, Set<String>> map) {
		if (!querySpec.getIncludedRelations().isEmpty()) {
			String key = addResourceType(RestrictedQueryParamsMembers.include, null, resourceType);

			StringBuilder builder = new StringBuilder();
			for (IncludeRelationSpec includedField : querySpec.getIncludedRelations()) {
				if (builder.length() > 0) {
					builder.append(",");
				}
				builder.append(StringUtils.join(".", includedField.getAttributePath()));
			}
			put(map, key, builder.toString());
		}
	}

	public void serializePagination(QuerySpec querySpec, String resourceType, Map<String, Set<String>> map) { // NOSONAR signature is ok
		if (querySpec.getOffset() != 0) {
			put(map, "page[offset]", Long.toString(querySpec.getOffset()));
		}
		if (querySpec.getLimit() != null) {
			put(map, "page[limit]", Long.toString(querySpec.getLimit()));
		}
	}

}
