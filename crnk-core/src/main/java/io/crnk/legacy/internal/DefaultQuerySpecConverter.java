package io.crnk.legacy.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.*;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.RestrictedPaginationKeys;
import io.crnk.legacy.queryParams.RestrictedSortingValues;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.*;

import java.util.*;
import java.util.Map.Entry;

@Deprecated
public class DefaultQuerySpecConverter implements QuerySpecConverter {

	private ResourceRegistry resourceRegistry;

	private TypeParser typeParser;

	private DefaultQuerySpecDeserializer deserializer;

	public DefaultQuerySpecConverter(ModuleRegistry moduleRegistry) {
		this.resourceRegistry = moduleRegistry.getResourceRegistry();
		this.typeParser = moduleRegistry.getTypeParser();
		this.deserializer = new DefaultQuerySpecDeserializer();
	}

	@Override
	public QuerySpec fromParams(Class<?> rootType, QueryParams params) {
		QuerySpec querySpec = new QuerySpec(rootType);
		applyIncludedFields(querySpec, params);
		applySorting(querySpec, params);
		applyFiltering(querySpec, params);
		applyRelatedFields(querySpec, params);
		applyPaging(querySpec, params);
		return querySpec;
	}

	private Class<?> getResourceClass(String resourceType) {
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceType);
		if (registryEntry == null) {
			throw new IllegalArgumentException("resourceType " + resourceType + " not found");
		}
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		return resourceInformation.getResourceClass();
	}

	protected void applyPaging(QuerySpec rootQuerySpec, QueryParams queryParams) {
		Map<RestrictedPaginationKeys, Integer> pagination = queryParams.getPagination();
		if (pagination != null) {
			for (Map.Entry<RestrictedPaginationKeys, Integer> entry : pagination.entrySet()) {
				RestrictedPaginationKeys key = entry.getKey();
				if (key == RestrictedPaginationKeys.limit) {
					rootQuerySpec.setLimit(entry.getValue().longValue());
				} else if (key == RestrictedPaginationKeys.offset) {
					rootQuerySpec.setOffset(entry.getValue());
				} else {
					throw new UnsupportedOperationException("not supported: " + key);
				}
			}
		}
	}

	protected void applyFiltering(QuerySpec rootQuerySpec, QueryParams queryParams) {
		// filtering
		TypedParams<FilterParams> filters = queryParams.getFilters();
		if (filters != null && !filters.getParams().isEmpty()) {

			for (Map.Entry<String, FilterParams> typeEntry : filters.getParams().entrySet()) {
				FilterParams filterParams = typeEntry.getValue();
				Class<?> resourceClass = getResourceClass(typeEntry.getKey());
				QuerySpec querySpec = rootQuerySpec.getOrCreateQuerySpec(resourceClass);
				for (Entry<String, Set<String>> entry : filterParams.getParams().entrySet()) {
					String pathString = entry.getKey();
					Set<String> stringValues = entry.getValue();
					applyFilter(querySpec, pathString, stringValues);
				}
			}
		}
	}

	private void applyFilter(QuerySpec querySpec, String parameterName, Set<String> stringValues) {
		// find operation
		FilterOperator filterOp = null;
		String attributePathString = parameterName;
		for (FilterOperator op : deserializer.getSupportedOperators()) {
			String opSuffix = "." + op.toString().toLowerCase().replace("_", "");
			if (parameterName.toLowerCase().endsWith(opSuffix)) {
				attributePathString = parameterName.substring(0, parameterName.length() - opSuffix.length());
				filterOp = op;
				break;
			}
		}
		if (filterOp == null) {
			filterOp = deserializer.getDefaultOperator();
		}

		List<String> attributePath = splitPath(attributePathString);

		Class<?> attributeType = PropertyUtils.getPropertyClass(querySpec.getResourceClass(), attributePath);
		Set<Object> typedValues = new HashSet<>();
		for (String stringValue : stringValues) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			Object value = typeParser.parse(stringValue, (Class) attributeType);
			typedValues.add(value);
		}
		Object value = typedValues.size() == 1 ? typedValues.iterator().next() : typedValues;

		querySpec.addFilter(new FilterSpec(attributePath, filterOp, value));
	}

	private List<String> splitPath(String pathString) {
		return Arrays.asList(pathString.split("\\."));
	}

	protected void applySorting(QuerySpec rootQuerySpec, QueryParams queryParams) {
		TypedParams<SortingParams> sorting = queryParams.getSorting();
		if (sorting != null && !sorting.getParams().isEmpty()) {

			for (Map.Entry<String, SortingParams> typeEntry : sorting.getParams().entrySet()) {
				SortingParams sortingParams = typeEntry.getValue();
				Class<?> resourceClass = getResourceClass(typeEntry.getKey());
				QuerySpec querySpec = rootQuerySpec.getOrCreateQuerySpec(resourceClass);

				for (Map.Entry<String, RestrictedSortingValues> entry : sortingParams.getParams().entrySet()) {
					Direction dir = entry.getValue() == RestrictedSortingValues.desc ? Direction.DESC : Direction.ASC;
					List<String> attributePath = splitPath(entry.getKey());
					querySpec.addSort(new SortSpec(attributePath, dir));
				}
			}
		}
	}

	protected void applyIncludedFields(QuerySpec rootQuerySpec, QueryParams queryParams) {
		TypedParams<IncludedFieldsParams> includes = queryParams.getIncludedFields();
		if (includes != null && !includes.getParams().isEmpty()) {
			for (Entry<String, IncludedFieldsParams> typeEntry : includes.getParams().entrySet()) {
				IncludedFieldsParams includeParams = typeEntry.getValue();
				Class<?> resourceClass = getResourceClass(typeEntry.getKey());
				QuerySpec querySpec = rootQuerySpec.getOrCreateQuerySpec(resourceClass);

				for (String inclusion : includeParams.getParams()) {
					List<String> attributePath = splitPath(inclusion);
					querySpec.includeField(attributePath);
				}
			}
		}
	}

	protected void applyRelatedFields(QuerySpec rootQuerySpec, QueryParams queryParams) {
		TypedParams<IncludedRelationsParams> includes = queryParams.getIncludedRelations();
		if (includes != null && !includes.getParams().isEmpty()) {

			for (Entry<String, IncludedRelationsParams> typeEntry : includes.getParams().entrySet()) {
				IncludedRelationsParams includeParams = typeEntry.getValue();
				Class<?> resourceClass = getResourceClass(typeEntry.getKey());
				QuerySpec querySpec = rootQuerySpec.getOrCreateQuerySpec(resourceClass);

				for (Inclusion inclusion : includeParams.getParams()) {
					List<String> attributePath = splitPath(inclusion.getPath());
					querySpec.includeRelation(attributePath);
				}
			}
		}
	}

}
