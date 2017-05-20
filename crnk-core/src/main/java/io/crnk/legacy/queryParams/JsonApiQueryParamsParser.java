package io.crnk.legacy.queryParams;

import io.crnk.core.resource.RestrictedQueryParamsMembers;
import io.crnk.legacy.queryParams.context.QueryParamsParserContext;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.SortingParams;
import io.crnk.legacy.queryParams.params.TypedParams;

import java.util.*;

/**
 * A {@link QueryParamsParser} implementation which adheres to the JSON-API
 * standard more strictly than the DefaultQueryParamsParser, at the expense of
 * having less flexibility.
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public class JsonApiQueryParamsParser extends DefaultQueryParamsParser {

	private static final String JSON_API_SORT_INDICATOR_DESC = "-";
	private static final String JSON_API_PARAM_DELIMITER = ",";

	/**
	 * Returns a list of all of the strings contained in parametersToParse. If
	 * any of the strings contained in parametersToParse is a comma-delimited
	 * list, that string will be split into substrings and each substring will
	 * be added to the returned set (in place of the delimited list).
	 */
	private static Set<String> parseDelimitedParameters(Set<String> parametersToParse) {
		Set<String> parsedParameters = new LinkedHashSet<>();
		if (parametersToParse != null && !parametersToParse.isEmpty()) {
			for (String parameterToParse : parametersToParse) {
				parsedParameters.addAll(Arrays.asList(parameterToParse.split(JSON_API_PARAM_DELIMITER)));
			}
		}
		return parsedParameters;
	}

	protected TypedParams<SortingParams> parseSortingParameters(final QueryParamsParserContext context) {
		String sortingKey = RestrictedQueryParamsMembers.sort.name();
		Set<String> rawSortingQueryParams = parseDelimitedParameters(context.getParameterValue(sortingKey));
		Map<String, SortingParams> decodedSortingMap = new LinkedHashMap<>();

		if (!rawSortingQueryParams.isEmpty()) {
			Map<String, RestrictedSortingValues> temporarySortingMap = new LinkedHashMap<>();

			for (String sortParam : rawSortingQueryParams) {
				if (sortParam.startsWith(JSON_API_SORT_INDICATOR_DESC)) {
					temporarySortingMap.put(sortParam.substring(1), RestrictedSortingValues.desc);
				} else {
					temporarySortingMap.put(sortParam, RestrictedSortingValues.asc);
				}
			}

			decodedSortingMap.put(context.getRequestedResourceInformation().getResourceType(), new SortingParams(temporarySortingMap));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedSortingMap));
	}

	protected TypedParams<IncludedRelationsParams> parseIncludedRelationsParameters(QueryParamsParserContext context) {
		String includeKey = RestrictedQueryParamsMembers.include.name();
		Map<String, Set<String>> inclusions = filterQueryParamsByKey(context, includeKey);

		Map<String, IncludedRelationsParams> decodedInclusions = new LinkedHashMap<>();

		if (inclusions.containsKey(RestrictedQueryParamsMembers.include.name())) {
			Set<Inclusion> inclusionSet = new LinkedHashSet<>();
			for (String inclusion : inclusions.get(RestrictedQueryParamsMembers.include.name())) {
				inclusionSet.add(new Inclusion(inclusion));
			}
			decodedInclusions.put(context.getRequestedResourceInformation().getResourceType(), new IncludedRelationsParams(inclusionSet));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedInclusions));
	}

	/**
	 * Filters provided query params to one starting with provided string key.
	 * This override also splits param values if they are contained in a
	 * comma-delimited list.
	 *
	 * @param context  used to inspect the parameters of the current request
	 * @param queryKey Filtering key
	 * @return Filtered query params
	 */
	@Override
	protected Map<String, Set<String>> filterQueryParamsByKey(QueryParamsParserContext context, String queryKey) {
		Map<String, Set<String>> filteredQueryParams = new HashMap<>();

		for (String paramName : context.getParameterNames()) {
			if (paramName.startsWith(queryKey)) {
				filteredQueryParams.put(paramName, parseDelimitedParameters(context.getParameterValue(paramName)));
			}
		}
		return filteredQueryParams;
	}
}