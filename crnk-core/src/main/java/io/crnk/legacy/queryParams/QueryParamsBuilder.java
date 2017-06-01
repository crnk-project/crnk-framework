package io.crnk.legacy.queryParams;

import io.crnk.core.exception.CrnkException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.legacy.queryParams.context.QueryParamsParserContext;
import io.crnk.legacy.queryParams.context.SimpleQueryParamsParserContext;

import java.util.Map;
import java.util.Set;

/**
 * Builder responsible for building queryParams. The legacy parsing is being delegated to a parser implementation.
 * The created {@link QueryParams} object contains several fields where each of them is not-null only when
 * this legacy has been passed with a request.
 * <p>
 * ---------------------------------------------------------------------------------------------------------------------
 * POTENTIAL IMPROVEMENT NOTE : This can be made even more flexible by implementing the builder pattern to allow
 * provisioning of different parsers for each component as the QueryParamsBuilder is being built: I.e:
 * QueryParamsBuilder.builder().filters(myCustomFilterParser).sorting(myOtherCustomSortingParser)...build()
 * This way, the user can mix and match various parsing strategies for individual components.
 * QueryParamsParser could become a one method interface and this could be particularly useful to Java 8 users who
 * can simply pass instances of java.lang.Function to implement custom parsing per component (filter/sort/group/etc etc).
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public class QueryParamsBuilder {

	private final QueryParamsParser queryParamsParser;

	public QueryParamsBuilder(final QueryParamsParser queryParamsParser) {
		this.queryParamsParser = queryParamsParser;
	}

	/**
	 * Decodes passed query parameters using the given raw map.  Mainly intended to be used for testing purposes.
	 * For most cases, use {@link #buildQueryParams(QueryParamsParserContext context) instead.}
	 *
	 * @param queryParams Map of provided query params
	 * @return QueryParams containing filtered query params grouped by JSON:API standard
	 * @throws ParametersDeserializationException thrown when unsupported input format is detected
	 */
	public QueryParams buildQueryParams(Map<String, Set<String>> queryParams) {
		return buildQueryParams(new SimpleQueryParamsParserContext(queryParams));
	}

	/**
	 * Parses the query parameters of the current request using this builder's QueryParamsParser and the
	 * given context.
	 *
	 * @param context - Contains raw information about the query parameters of the current request
	 * @return - QueryParams object which contains the parsed query parameters of the current request
	 * @throws ParametersDeserializationException thrown when unsupported input format is detected
	 */
	public QueryParams buildQueryParams(QueryParamsParserContext context) {
		try {
			return queryParamsParser.parse(context);
		} catch (CrnkException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new ParametersDeserializationException(e.getMessage(), e);
		}
	}
}
