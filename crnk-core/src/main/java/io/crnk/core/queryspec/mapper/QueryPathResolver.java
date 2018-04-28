package io.crnk.core.queryspec.mapper;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.QuerySpec;

import java.util.List;

public interface QueryPathResolver {

	enum NamingType {
		JAVA,
		JSON
	}

	void init(QuerySpecUrlContext context);

	/**
	 * Translates a parameter path to/from JSON for QuerySpec-related operations.
	 *
	 * @param resourceInformation
	 * @param attributePath
	 * @param sourceParameter
	 * @return
	 */
	QueryPathSpec resolve(ResourceInformation resourceInformation, List<String> attributePath, NamingType sourceNamingType, String sourceParameter);

	/**
	 * @return whether to allow to pass unknown paths in sort, filter, include and field parameters. Disabled by default.
	 */
	boolean getAllowUnknownAttributes();

	void setAllowUnknownAttributes(boolean allowUnknownAttributes);

	/**
	 * @return whether to map json to java names in {@link QuerySpec} for sort, filter, include and field parameters. True by default.
	 */
	boolean getMapJsonNames();

	void setMapJsonNames(boolean mapJsonNames);
}