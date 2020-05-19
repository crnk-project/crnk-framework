package io.crnk.core.queryspec;

import java.lang.reflect.Type;
import java.util.Collection;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.mapper.QueryParameter;

/**
 * Filter operator used to compare attributes to values by {@link FilterSpec}.
 */
@JsonSerialize(using = ToStringSerializer.class)
public class FilterOperator {

	/**
	 * Boolean and
	 */
	public static final FilterOperator AND = new FilterOperator("AND") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * Selection of facet for nested filtering
	 */
	public static final FilterOperator SELECT = new FilterOperator("SELECT") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * Grouping based on facet
	 */
	public static final FilterOperator GROUP = new FilterOperator("GROUP") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};


	/**
	 * Like operation. In case of in-memory filtering it makes use of "%" as
	 * wildcard and is case-insenstive.
	 * <p>
	 * Application may implement their own matches() method to match the
	 * filtering behavior of the used storage backend (like SQL).
	 */
	public static final FilterOperator LIKE = new FilterOperator("LIKE") {

		@Override
		public Type getFilterType(QueryParameter queryParameter, Type attributeType) {
			return String.class;
		}

		@Override
		public boolean matches(Object value, Object likePattern) {
			if (likePattern == null) {
				throw new BadRequestException("LIKE pattern cannot be null");
			}
			if (value == null) {
				return false;
			}
			String text = value.toString();

			// translate queryterm to a regex pattern
			char[] queryTerm = likePattern.toString().toCharArray();

			StringBuilder regex = new StringBuilder();
			regex.append(".*");
			String escapedCharacters = "[\\^$.|?*+()";
			for (char c : queryTerm) {
				if (escapedCharacters.contains(Character.toString(c))) {
					regex.append('\\');
					regex.append(c);
				} else if (c == '%') {
					regex.append(".*");
				} else {
					regex.append(Character.toLowerCase(c));
				}
			}
			regex.append(".*");

			return text.toLowerCase().matches(regex.toString());
		}

	};

	/**
	 * Boolean or
	 */
	public static final FilterOperator OR = new FilterOperator("OR") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * Boolean not
	 */
	public static final FilterOperator NOT = new FilterOperator("NOT") {

		@Override
		public boolean matches(Object value1, Object value2) {
			throw new UnsupportedOperationException(); // handle differently
		}

	};

	/**
	 * equals
	 */
	public static final FilterOperator EQ = new FilterOperator("EQ") {

		@Override
		public boolean matches(Object value1, Object value2) {
			if (value2 instanceof Collection) {
				return ((Collection<?>) value2).contains(value1);
			}
			return CompareUtils.isEquals(value1, value2);
		}

	};

	/**
	 * like with * as wildcard
	 */
	public static final FilterOperator LT = new FilterOperator("LT") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1.compareTo(c2) < 0;
		}

	};

	/**
	 * less equals
	 */
	public static final FilterOperator LE = new FilterOperator("LE") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1 != null && c1.compareTo(c2) <= 0;
		}

	};

	/**
	 * greater
	 */
	public static final FilterOperator GT = new FilterOperator("GT") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1 != null && c1.compareTo(c2) > 0;
		}
	};

	/**
	 * greater equals
	 */
	public static final FilterOperator GE = new FilterOperator("GE") {

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object value1, Object value2) {
			Comparable<Object> c1 = (Comparable<Object>) value1;
			Comparable<Object> c2 = (Comparable<Object>) value2;
			return c1 != null && c1 != null && c1.compareTo(c2) >= 0;
		}
	};

	/**
	 * not equals
	 */
	public static final FilterOperator NEQ = new FilterOperator("NEQ") {

		@Override
		public boolean matches(Object value1, Object value2) {
			if (value2 instanceof Collection) {
				return !((Collection<?>) value2).contains(value1);
			}
			return !CompareUtils.isEquals(value1, value2);
		}
	};

	private final String id;

	protected FilterOperator(String id) {
		this.id = id;
	}

	public String name() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof FilterOperator) {
			FilterOperator other = (FilterOperator) obj;
			return CompareUtils.isEquals(id, other.id);
		}
		return false;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getName() {
		return id;
	}

	/**
	 * Performs a in-memory evaluation of the operator on the given to values.
	 *
	 * @param value1 first value
	 * @param value2 second value
	 * @return true if matches
	 */
	public  boolean matches(Object value1, Object value2){
		throw new UnsupportedOperationException("not implemented");
	}

	/**
	 * Typically the type of a filter parameter and the type of an attribute match. But some operators like LIKE have a type oder
	 * than Enum (such as String for LIKE).
	 *
	 * @param attributeType type of the attribute to be filtered
	 * @return type of the filter parameter.
	 */
	public Type getFilterType(QueryParameter queryParameter, Type attributeType) {
		return attributeType;
	}
}
