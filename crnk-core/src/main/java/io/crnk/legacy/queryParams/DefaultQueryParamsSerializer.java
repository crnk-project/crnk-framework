package io.crnk.legacy.queryParams;

import io.crnk.core.resource.RestrictedQueryParamsMembers;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @deprecated make use of QuerySpec
 */
@Deprecated
public class DefaultQueryParamsSerializer implements QueryParamsSerializer {

	@Override
	public Map<String, Set<String>> serializeFilters(QueryParams queryParams) {
		Map<String, Set<String>> map = new HashMap<>();
		TypedParams<FilterParams> filters = queryParams.getFilters();
		if (filters != null) {
			Map<String, FilterParams> filterParamsMap = filters.getParams();
			for (Map.Entry<String, FilterParams> entry : filterParamsMap.entrySet()) {
				String type = entry.getKey();
				Map<String, Set<String>> params = entry.getValue().getParams();
				for (Map.Entry<String, Set<String>> param : params.entrySet()) {
					String paramName = RestrictedQueryParamsMembers.filter.name() + "[" + type + "]" + serializeKey(param.getKey());
					map.put(paramName, param.getValue());
				}
			}
		}
		return map;
	}

	private String serializeKey(String key) {
		return "[" + key.replace(".", "][") + "]";
	}

	@Override
	public Map<String, String> serializeSorting(QueryParams queryParams) {
		Map<String, String> map = new HashMap<>();
		TypedParams<SortingParams> sorting = queryParams.getSorting();
		if (sorting != null) {
			Map<String, SortingParams> sortingParamsMap = sorting.getParams();
			for (Map.Entry<String, SortingParams> entry : sortingParamsMap.entrySet()) {
				String type = entry.getKey();
				Map<String, RestrictedSortingValues> params = entry.getValue().getParams();
				for (Entry<String, RestrictedSortingValues> param : params.entrySet()) {
					String paramName = RestrictedQueryParamsMembers.sort.name() + "[" + type + "][" + param.getKey() + "]";
					map.put(paramName, param.getValue().toString());
				}
			}
		}
		return map;
	}

	@Override
	public Map<String, Set<String>> serializeIncludedFields(QueryParams queryParams) {
		Map<String, Set<String>> map = new HashMap<>();
		TypedParams<IncludedFieldsParams> includedFields = queryParams.getIncludedFields();
		if (includedFields != null) {
			Map<String, IncludedFieldsParams> includedFieldsParamsMap = includedFields.getParams();
			for (Entry<String, IncludedFieldsParams> entry : includedFieldsParamsMap.entrySet()) {
				String type = entry.getKey();
				Set<String> values = entry.getValue().getParams();
				String paramName = RestrictedQueryParamsMembers.fields.name() + "[" + type + "]";
				map.put(paramName, values);
			}
		}
		return map;
	}

	@Override
	public Map<String, Set<String>> serializeIncludedRelations(QueryParams queryParams) {
		Map<String, Set<String>> map = new HashMap<>();
		TypedParams<IncludedRelationsParams> includedRelations = queryParams.getIncludedRelations();
		if (includedRelations != null) {
			Map<String, IncludedRelationsParams> includedRelationsParamsMap = includedRelations.getParams();
			for (Entry<String, IncludedRelationsParams> entry : includedRelationsParamsMap.entrySet()) {
				String type = entry.getKey();
				Set<Inclusion> inclusions = entry.getValue().getParams();

				Set<String> strInclusions = new HashSet<>();
				for (Inclusion inclusion : inclusions) {
					strInclusions.add(inclusion.getPath());
				}

				String paramName = RestrictedQueryParamsMembers.include.name() + "[" + type + "]";
				map.put(paramName, strInclusions);
			}
		}
		return map;
	}

	@Override
	public Map<String, String> serializePagination(QueryParams queryParams) {
		Map<String, String> map = new HashMap<>();
		Map<RestrictedPaginationKeys, Integer> pagination = queryParams.getPagination();
		if (pagination != null) {
			for (Entry<RestrictedPaginationKeys, Integer> entry : pagination.entrySet()) {
				String paramName = RestrictedQueryParamsMembers.page.name() + "[" + entry.getKey().name() + "]";
				map.put(paramName, entry.getValue().toString());
			}
		}
		return map;
	}

	@Override
	public Map<String, Set<String>> serializeGrouping(QueryParams queryParams) {
		// not supported so far
		return new HashMap<>();
	}
}
