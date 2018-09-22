package io.crnk.core.queryspec;

import io.crnk.core.engine.internal.utils.CompareUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FilterSpec extends AbstractPathSpec implements Comparable<FilterSpec> {

	private FilterOperator operator;
	private Object value;
	private List<FilterSpec> expressions;

	protected FilterSpec() {
	}

	FilterSpec(FilterSpec spec) {
		super(spec.getAttributePath());
		this.operator = spec.operator;
		this.value = spec.value;
		if (spec.expressions != null) {
			this.expressions = cloneExpressions(spec.expressions, false);
		}
	}

	public FilterSpec(FilterOperator operator, List<FilterSpec> expressions) {
		super();
		this.operator = operator;
		this.expressions = expressions;
	}

	public FilterSpec(List<String> attributePath, FilterOperator operator, Object value) {
		this(PathSpec.of(attributePath), operator, value);
	}

	public FilterSpec(PathSpec path, FilterOperator operator, Object value) {
		super(path);
		this.operator = operator;
		this.value = value;
		assertOperator();
		assertNotExpressions();
	}

	private FilterSpec(PathSpec pathSpec, FilterOperator operator, Object value, List<FilterSpec> expressions) {
		super(pathSpec);
		this.operator = operator;
		this.value = value;
		this.expressions = expressions;
	}

	public static FilterSpec and(Collection<FilterSpec> conditions) {
		return and(conditions.toArray(new FilterSpec[conditions.size()]));
	}

	public static FilterSpec and(FilterSpec... conditions) {
		if (conditions.length == 1) {
			return conditions[0];
		}
		FilterSpec ret = new FilterSpec();
		ret.setOperator(FilterOperator.AND);
		for (FilterSpec c : conditions) {
			ret.addExpression(c);
		}
		return ret;
	}

	public static FilterSpec or(Collection<FilterSpec> conditions) {
		return or(conditions.toArray(new FilterSpec[conditions.size()]));
	}

	public static FilterSpec or(FilterSpec... conditions) {
		if (conditions.length == 1) {
			return conditions[0];
		}
		FilterSpec ret = new FilterSpec();
		ret.setOperator(FilterOperator.OR);
		for (FilterSpec c : conditions) {
			ret.addExpression(c);
		}
		return ret;
	}

	public static FilterSpec not(FilterSpec expression) {
		FilterSpec ret = new FilterSpec();
		ret.setOperator(FilterOperator.NOT);
		ret.addExpression(expression);
		return ret;
	}

	static List<FilterSpec> cloneExpressions(List<FilterSpec> list, boolean normalize) {
		List<FilterSpec> result = new ArrayList<>();
		for (FilterSpec spec : list) {
			if (normalize) {
				result.add(spec.normalize());
			} else {
				result.add(spec.clone());
			}
		}
		if (normalize) {
			Collections.sort(result);
		}
		return result;
	}

	private void assertOperator() {
		if (operator == null) {
			throw new IllegalArgumentException("Condition required");
		}
	}

	private void assertNotExpressions() {
		if (operator == FilterOperator.NOT) {
			throw new IllegalArgumentException("NOT operator not allowed when comparing with a value, use NOT_EQUAL");
		}
		if (operator == FilterOperator.AND || operator == FilterOperator.OR) {
			throw new IllegalArgumentException(operator + " operator not allowed when comparing with a value");
		}
	}

	public FilterOperator getOperator() {
		return operator;
	}

	public void setOperator(FilterOperator condition) {
		this.operator = condition;
	}

	public <T> T getValue() {
		return (T) value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public List<FilterSpec> getExpression() {
		return expressions;
	}

	/**
	 * Adds the given expression to the expression list and returns itself.
	 *
	 * @param expr expression
	 * @return this
	 */
	public FilterSpec addExpression(FilterSpec expr) {
		if (expressions == null) {
			expressions = new ArrayList<>();
		}
		expressions.add(expr);
		return this;
	}

	public boolean hasExpressions() {
		// if nothing is set we assume an empty expression (i.e. an empty where
		// clause)
		return getAttributePath() == null;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[]{getAttributePath(), operator, expressions, value});
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		FilterSpec other = (FilterSpec) obj;
		return CompareUtils.isEquals(path, other.path)
				&& CompareUtils.isEquals(operator, other.operator) && CompareUtils.isEquals(value, other.value)
				&& CompareUtils.isEquals(expressions, other.expressions);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (getExpression() != null) {
			int nExprs = getExpression().size();
			if (getOperator() == FilterOperator.NOT) {
				appendNot(b, nExprs);
			} else {
				appendExpressions(b, nExprs);
			}
		} else if (path != null) {
			b.append(path);
			b.append(' ');
			b.append(operator.name());
			b.append(' ');
			b.append(value);
		}
		return b.toString();
	}

	private void appendExpressions(StringBuilder b, int nExprs) {
		for (int i = 0; i < nExprs; i++) {
			if (i > 0) {
				b.append(' ');
				b.append(getOperator());
				b.append(' ');
			}
			b.append('(');
			b.append(getExpression().get(i));
			b.append(')');
		}
	}

	private void appendNot(StringBuilder b, int nExprs) {
		b.append("NOT");
		if (nExprs > 1) {
			b.append('(');
		}
		for (int i = 0; i < nExprs; i++) {
			if (i > 0) {
				b.append(" AND ");
			}
			b.append('(');
			b.append(getExpression().get(i));
			b.append(')');
		}
		if (nExprs > 1) {
			b.append(')');
		}
	}

	/**
	 * Normalizes this FilterSpec by normalizing all FilterSpec objects within
	 * <code>expressions</code> and then sorting the list itself.
	 *
	 * @return normalized FilterSpec
	 */
	public FilterSpec normalize() {
		List<FilterSpec> clonedExpressions = expressions != null ? cloneExpressions(expressions, true) : null;

		FilterSpec copy = new FilterSpec(path, operator, value, clonedExpressions);
		return copy;
	}

	@Override
	public FilterSpec clone() { // NOSONAR
		return new FilterSpec(this);
	}

	@Override
	public int compareTo(FilterSpec o) {
		return toString().compareTo(o.toString());
	}
}
