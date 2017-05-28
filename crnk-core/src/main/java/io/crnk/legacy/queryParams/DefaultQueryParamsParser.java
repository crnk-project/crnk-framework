/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.crnk.legacy.queryParams;

import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.resource.RestrictedQueryParamsMembers;
import io.crnk.legacy.queryParams.context.QueryParamsParserContext;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The default QueryParamsParser implementation which parses query parameters with the behavior
 * specified in the Crnk documentation.  This parser does NOT adhere to the JSON-API specification,
 * but it does provide more flexibility.  If you need to adhere to the spec, use {@link JsonApiQueryParamsParser}
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public class DefaultQueryParamsParser implements QueryParamsParser {

	protected static List<String> buildPropertyListFromEntry(Map.Entry<String, Set<String>> entry, String prefix) {
		String entryKey = entry.getKey()
				.substring(prefix.length());

		String pattern = "[^\\]\\[]+(?<!\\[)(?=\\])";
		Pattern regexp = Pattern.compile(pattern);
		Matcher matcher = regexp.matcher(entryKey);
		List<String> matchList = new LinkedList<>();

		while (matcher.find()) {
			matchList.add(matcher.group());
		}

		if (matchList.isEmpty()) {
			throw new ParametersDeserializationException("Malformed filter legacy: " + entryKey);
		}

		return matchList;
	}

	/**
	 * <strong>Important!</strong> Crnk implementation differs form JSON API
	 * <a href="http://jsonapi.org/format/#fetching-filtering">definition of filtering</a>
	 * in order to fit standard query legacy serializing strategy and maximize effective processing of data.
	 * <p>
	 * Filter params can be send with following format (Crnk does not specify or implement any operators): <br>
	 * <strong>filter[ResourceType][property|operator]([property|operator])* = "value"</strong><br>
	 * <p>
	 * Examples of accepted filtering of resources:
	 * <ul>
	 * <li>{@code GET /tasks/?filter[tasks][name]=Super task}</li>
	 * <li>{@code GET /tasks/?filter[tasks][name]=Super task&filter[tasks][dueDate]=2015-10-01}</li>
	 * <li>{@code GET /tasks/?filter[tasks][name][$startWith]=Super task}</li>
	 * <li>{@code GET /tasks/?filter[tasks][name][][$startWith]=Super&filter[tasks][name][][$endWith]=task}</li>
	 * </ul>
	 *
	 * @param context No idea. I didn't write this code.
	 * @return {@link TypedParams} Map of filtering params passed to a request grouped by type of document
	 */
	protected TypedParams<FilterParams> parseFiltersParameters(final QueryParamsParserContext context) {
		String filterKey = RestrictedQueryParamsMembers.filter.name();
		Map<String, Set<String>> filters = filterQueryParamsByKey(context, filterKey);

		Map<String, Map<String, Set<String>>> temporaryFiltersMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : filters.entrySet()) {

			List<String> propertyList = buildPropertyListFromEntry(entry, filterKey);

			String resourceType = propertyList.get(0);
			String propertyPath = StringUtils.join(".", propertyList.subList(1, propertyList.size()));

			if (temporaryFiltersMap.containsKey(resourceType)) {
				Map<String, Set<String>> resourceParams = temporaryFiltersMap.get(resourceType);
				resourceParams.put(propertyPath, Collections.unmodifiableSet(entry.getValue()));
			} else {
				Map<String, Set<String>> resourceParams = new LinkedHashMap<>();
				temporaryFiltersMap.put(resourceType, resourceParams);
				resourceParams.put(propertyPath, entry.getValue());
			}
		}

		Map<String, FilterParams> decodedFiltersMap = new LinkedHashMap<>();

		for (Map.Entry<String, Map<String, Set<String>>> resourceTypesMap : temporaryFiltersMap.entrySet()) {
			Map<String, Set<String>> filtersMap = Collections.unmodifiableMap(resourceTypesMap.getValue());
			decodedFiltersMap.put(resourceTypesMap.getKey(), new FilterParams(filtersMap));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedFiltersMap));
	}

	/**
	 * <strong>Important!</strong> Crnk implementation differs form JSON API
	 * <a href="http://jsonapi.org/format/#fetching-sorting">definition of sorting</a>
	 * in order to fit standard query legacy serializing strategy and maximize effective processing of data.
	 * <p>
	 * Sort params can be send with following format: <br>
	 * <strong>sort[ResourceType][property]([property])* = "asc|desc"</strong>
	 * <p>
	 * Examples of accepted sorting of resources:
	 * <ul>
	 * <li>{@code GET /tasks/?sort[tasks][name]=asc}</li>
	 * <li>{@code GET /project/?sort[projects][shortName]=desc&sort[users][name][firstName]=asc}</li>
	 * </ul>
	 *
	 * @param context Don't know, didn't write the code
	 * @return {@link TypedParams} Map of sorting params passed to request grouped by type of document
	 */
	protected TypedParams<SortingParams> parseSortingParameters(final QueryParamsParserContext context) {
		String sortingKey = RestrictedQueryParamsMembers.sort.name();
		Map<String, Set<String>> sorting = filterQueryParamsByKey(context, sortingKey);

		Map<String, Map<String, RestrictedSortingValues>> temporarySortingMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : sorting.entrySet()) {

			List<String> propertyList = buildPropertyListFromEntry(entry, sortingKey);

			String resourceType = propertyList.get(0);
			String propertyPath = StringUtils.join(".", propertyList.subList(1, propertyList.size()));


			if (temporarySortingMap.containsKey(resourceType)) {
				Map<String, RestrictedSortingValues> resourceParams = temporarySortingMap.get(resourceType);
				resourceParams.put(propertyPath, RestrictedSortingValues.valueOf(entry.getValue()
						.iterator()
						.next()));
			} else {
				Map<String, RestrictedSortingValues> resourceParams = new HashMap<>();
				temporarySortingMap.put(resourceType, resourceParams);
				resourceParams.put(propertyPath, RestrictedSortingValues.valueOf(entry.getValue()
						.iterator()
						.next()));
			}
		}

		Map<String, SortingParams> decodedSortingMap = new LinkedHashMap<>();

		for (Map.Entry<String, Map<String, RestrictedSortingValues>> resourceTypesMap : temporarySortingMap.entrySet()) {
			Map<String, RestrictedSortingValues> sortingMap = Collections.unmodifiableMap(resourceTypesMap.getValue());
			decodedSortingMap.put(resourceTypesMap.getKey(), new SortingParams(sortingMap));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedSortingMap));
	}

	/**
	 * <strong>Important: </strong> Grouping itself is not specified by JSON API itself, but the
	 * keyword and format it reserved for today and future use in Crnk.
	 * <p>
	 * Group params can be send with following format: <br>
	 * <strong>group[ResourceType] = "property(.property)*"</strong>
	 * <p>
	 * Examples of accepted grouping of resources:
	 * <ul>
	 * <li>{@code GET /tasks/?group[tasks]=name}</li>
	 * <li>{@code GET /project/?group[users]=name.firstName&include[projects]=team}</li>
	 * </ul>
	 *
	 * @param context I don't know, I didn't write the code
	 * @return {@link Map} Map of grouping params passed to request grouped by type of document
	 */
	protected TypedParams<GroupingParams> parseGroupingParameters(final QueryParamsParserContext context) {
		String groupingKey = RestrictedQueryParamsMembers.group.name();
		Map<String, Set<String>> grouping = filterQueryParamsByKey(context, groupingKey);

		Map<String, Set<String>> temporaryGroupingMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : grouping.entrySet()) {

			List<String> propertyList = buildPropertyListFromEntry(entry, groupingKey);

			if (propertyList.size() > 1) {
				throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'group' legacy " +
						"(1) eg. group[tasks][name] <-- #2 level and more are not allowed");
			}

			String resourceType = propertyList.get(0);

			if (temporaryGroupingMap.containsKey(resourceType)) {
				Set<String> resourceParams = temporaryGroupingMap.get(resourceType);
				resourceParams.addAll(entry.getValue());
				temporaryGroupingMap.put(resourceType, resourceParams);
			} else {
				Set<String> resourceParams = new LinkedHashSet<>();
				resourceParams.addAll(entry.getValue());
				temporaryGroupingMap.put(resourceType, resourceParams);
			}
		}

		Map<String, GroupingParams> decodedGroupingMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> resourceTypesMap : temporaryGroupingMap.entrySet()) {
			Set<String> groupingSet = Collections.unmodifiableSet(resourceTypesMap.getValue());
			decodedGroupingMap.put(resourceTypesMap.getKey(), new GroupingParams(groupingSet));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedGroupingMap));
	}

	/**
	 * <strong>Important!</strong> Crnk implementation differs form JSON API
	 * <a href="http://jsonapi.org/format/#fetching-sparse-fieldsets">definition of sparse field set</a>
	 * in order to fit standard query legacy serializing strategy and maximize effective processing of data.
	 * <p>
	 * Sparse field set params can be send with following format: <br>
	 * <strong>fields[ResourceType] = "property(.property)*"</strong><br>
	 * <p>
	 * Examples of accepted sparse field sets of resources:
	 * <ul>
	 * <li>{@code GET /tasks/?fields[tasks]=name}</li>
	 * <li>{@code GET /tasks/?fields[tasks][]=name&fields[tasks][]=dueDate}</li>
	 * <li>{@code GET /tasks/?fields[users]=name.surname&include[tasks]=author}</li>
	 * </ul>
	 *
	 * @param context Don't know, didn't write the code
	 * @return {@link TypedParams} Map of sparse field set params passed to a request grouped by type of document
	 */
	protected TypedParams<IncludedFieldsParams> parseIncludedFieldsParameters(final QueryParamsParserContext context) {
		String sparseKey = RestrictedQueryParamsMembers.fields.name();
		Map<String, Set<String>> sparse = filterQueryParamsByKey(context, sparseKey);

		Map<String, Set<String>> temporarySparseMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : sparse.entrySet()) {
			List<String> propertyList = buildPropertyListFromEntry(entry, sparseKey);

			if (propertyList.size() > 1) {
				throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'fields' " +
						"legacy (1) eg. fields[tasks][name] <-- #2 level and more are not allowed");
			}

			String resourceType = propertyList.get(0);

			if (temporarySparseMap.containsKey(resourceType)) {
				Set<String> resourceParams = temporarySparseMap.get(resourceType);
				resourceParams.addAll(entry.getValue());
				temporarySparseMap.put(resourceType, resourceParams);
			} else {
				Set<String> resourceParams = new LinkedHashSet<>();
				resourceParams.addAll(entry.getValue());
				temporarySparseMap.put(resourceType, resourceParams);
			}
		}

		Map<String, IncludedFieldsParams> decodedSparseMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> resourceTypesMap : temporarySparseMap.entrySet()) {
			Set<String> sparseSet = Collections.unmodifiableSet(resourceTypesMap.getValue());
			decodedSparseMap.put(resourceTypesMap.getKey(), new IncludedFieldsParams(sparseSet));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedSparseMap));
	}

	/**
	 * <strong>Important!</strong> Crnk implementation differs form JSON API
	 * <a href="http://jsonapi.org/format/#fetching-includes">definition of includes</a>
	 * in order to fit standard query legacy serializing strategy and maximize effective processing of data.
	 * <p>
	 * Included field set params can be send with following format: <br>
	 * <strong>include[ResourceType] = "property(.property)*"</strong><br>
	 * <p>
	 * Examples of accepted sparse field sets of resources:
	 * <ul>
	 * <li>{@code GET /tasks/?include[tasks]=author}</li>
	 * <li>{@code GET /tasks/?include[tasks][]=author&include[tasks][]=comments}</li>
	 * <li>{@code GET /projects/?include[projects]=task&include[tasks]=comments}</li>
	 * </ul>
	 *
	 * @param context Don't know, didn't write the code
	 * @return {@link TypedParams} Map of sparse field set params passed to a request grouped by type of document
	 */
	protected TypedParams<IncludedRelationsParams> parseIncludedRelationsParameters(QueryParamsParserContext context) {
		String includeKey = RestrictedQueryParamsMembers.include.name();
		Map<String, Set<String>> inclusions = filterQueryParamsByKey(context, includeKey);

		Map<String, Set<Inclusion>> temporaryInclusionsMap = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : inclusions.entrySet()) {
			List<String> propertyList = buildPropertyListFromEntry(entry, includeKey);

			if (propertyList.size() > 1) {
				throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'include' " +
						"legacy (1)");
			}

			String resourceType = propertyList.get(0);
			Set<Inclusion> resourceParams;
			if (temporaryInclusionsMap.containsKey(resourceType)) {
				resourceParams = temporaryInclusionsMap.get(resourceType);
			} else {
				resourceParams = new LinkedHashSet<>();
			}
			for (String path : entry.getValue()) {
				resourceParams.add(new Inclusion(path));
			}
			temporaryInclusionsMap.put(resourceType, resourceParams);
		}

		Map<String, IncludedRelationsParams> decodedInclusions = new LinkedHashMap<>();

		for (Map.Entry<String, Set<Inclusion>> resourceTypesMap : temporaryInclusionsMap.entrySet()) {
			Set<Inclusion> inclusionSet = Collections.unmodifiableSet(resourceTypesMap.getValue());
			decodedInclusions.put(resourceTypesMap.getKey(), new IncludedRelationsParams(inclusionSet));
		}

		return new TypedParams<>(Collections.unmodifiableMap(decodedInclusions));
	}

	/**
	 * <strong>Important!</strong> Crnk implementation sets on strategy of pagination whereas JSON API
	 * <a href="http://jsonapi.org/format/#fetching-pagination">definition of pagination</a>
	 * is agnostic about pagination strategies.
	 * <p>
	 * Pagination params can be send with following format: <br>
	 * <strong>page[offset|limit] = "value"</strong>, where value is an integer
	 * <p>
	 * Examples of accepted grouping of resources:
	 * <ul>
	 * <li>{@code GET /projects/?page[offset]=0&page[limit]=10}</li>
	 * </ul>
	 *
	 * @param context Don't know, didn't write the code
	 * @param context Ask PJ about this
	 * @return {@link Map} Map of pagination keys passed to request
	 */
	protected Map<RestrictedPaginationKeys, Integer> parsePaginationParameters(final QueryParamsParserContext context) {
		String pagingKey = RestrictedQueryParamsMembers.page.name();
		Map<String, Set<String>> pagination = filterQueryParamsByKey(context, pagingKey);

		Map<RestrictedPaginationKeys, Integer> decodedPagination = new LinkedHashMap<>();

		for (Map.Entry<String, Set<String>> entry : pagination.entrySet()) {
			List<String> propertyList = buildPropertyListFromEntry(entry, RestrictedQueryParamsMembers.page.name());

			if (propertyList.size() > 1) {
				throw new ParametersDeserializationException("Exceeded maximum level of nesting of 'page' legacy " +
						"(1) eg. page[offset][minimal] <-- #2 level and more are not allowed");
			}

			String resourceType = propertyList.get(0);

			decodedPagination.put(RestrictedPaginationKeys.valueOf(resourceType), Integer.parseInt(entry
					.getValue()
					.iterator()
					.next()));
		}

		return Collections.unmodifiableMap(decodedPagination);
	}

	/**
	 * Filters provided query params to one starting with provided string key
	 *
	 * @param context  used to inspect the parameters of the current request
	 * @param queryKey Filtering key
	 * @return Filtered query params
	 */
	protected Map<String, Set<String>> filterQueryParamsByKey(QueryParamsParserContext context, String queryKey) {
		Map<String, Set<String>> filteredQueryParams = new HashMap<>();

		for (String paramName : context.getParameterNames()) {
			if (paramName.startsWith(queryKey)) {
				filteredQueryParams.put(paramName, context.getParameterValue(paramName));
			}
		}
		return filteredQueryParams;
	}

	@Override
	public QueryParams parse(QueryParamsParserContext context) {
		QueryParams queryParams = new QueryParams();
		queryParams.setFilters(parseFiltersParameters(context));
		queryParams.setSorting(parseSortingParameters(context));
		queryParams.setGrouping(parseGroupingParameters(context));
		queryParams.setPagination(parsePaginationParameters(context));
		queryParams.setIncludedFields(parseIncludedFieldsParameters(context));
		queryParams.setIncludedRelations(parseIncludedRelationsParameters(context));
		return queryParams;
	}
}
