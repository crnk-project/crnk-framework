package io.crnk.legacy.queryParams;

import io.crnk.legacy.queryParams.params.FilterParams;
import io.crnk.legacy.queryParams.params.GroupingParams;
import io.crnk.legacy.queryParams.params.IncludedFieldsParams;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.SortingParams;
import io.crnk.legacy.queryParams.params.TypedParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Contains a set of parameters passed along with the request.
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public class QueryParams {

	private TypedParams<FilterParams> filters;

	private TypedParams<SortingParams> sorting;

	private TypedParams<GroupingParams> grouping;

	private TypedParams<IncludedFieldsParams> includedFields;

	private TypedParams<IncludedRelationsParams> includedRelations;

	private Map<RestrictedPaginationKeys, Integer> pagination;

	private static boolean warningShown = false;

	public QueryParams() {
		if (!warningShown) {
			warningShown = true;
			Logger logger = LoggerFactory.getLogger(QueryParams.class);
			logger.warn("QueryParams is deprecated an will be removed in the future.");
		}

	}

	public TypedParams<FilterParams> getFilters() {
		return filters;
	}

	void setFilters(TypedParams<FilterParams> filters) {
		this.filters = filters;
	}

	public TypedParams<SortingParams> getSorting() {
		return sorting;
	}

	void setSorting(TypedParams<SortingParams> sorting) {
		this.sorting = sorting;
	}

	public TypedParams<GroupingParams> getGrouping() {
		return grouping;
	}

	void setGrouping(TypedParams<GroupingParams> grouping) {
		this.grouping = grouping;
	}

	public Map<RestrictedPaginationKeys, Integer> getPagination() {
		return pagination;
	}

	void setPagination(Map<RestrictedPaginationKeys, Integer> pagination) {
		this.pagination = pagination;
	}

	public TypedParams<IncludedFieldsParams> getIncludedFields() {
		return includedFields;
	}

	void setIncludedFields(TypedParams<IncludedFieldsParams> includedFields) {
		this.includedFields = includedFields;
	}

	public TypedParams<IncludedRelationsParams> getIncludedRelations() {
		return includedRelations;
	}

	void setIncludedRelations(TypedParams<IncludedRelationsParams> includedRelations) {
		this.includedRelations = includedRelations;
	}

	@Override
	public int hashCode() {
		int result = filters != null ? filters.hashCode() : 0;
		result = 31 * result + (sorting != null ? sorting.hashCode() : 0);
		result = 31 * result + (grouping != null ? grouping.hashCode() : 0);
		result = 31 * result + (includedFields != null ? includedFields.hashCode() : 0);
		result = 31 * result + (includedRelations != null ? includedRelations.hashCode() : 0);
		result = 31 * result + (pagination != null ? pagination.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		QueryParams that = (QueryParams) o;

		if (filters != null ? !filters.equals(that.filters) : that.filters != null) {
			return false;
		}
		if (sorting != null ? !sorting.equals(that.sorting) : that.sorting != null) {
			return false;
		}
		if (grouping != null ? !grouping.equals(that.grouping) : that.grouping != null) {
			return false;
		}
		if (includedFields != null ? !includedFields.equals(that.includedFields) : that.includedFields != null) {
			return false;
		}
		if (includedRelations != null ? !includedRelations.equals(that.includedRelations) : that.includedRelations != null) {
			return false;
		}
		return pagination != null ? pagination.equals(that.pagination) : that.pagination == null;
	}

	@Override
	public String toString() {
		return "QueryParams{" +
				"filters=" + filters +
				", sorting=" + sorting +
				", grouping=" + grouping +
				", includedFields=" + includedFields +
				", includedRelations=" + includedRelations +
				", pagination=" + pagination +
				'}';
	}
}