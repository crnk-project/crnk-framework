package io.crnk.core.engine.filter;

public enum FilterBehavior {

	/**
	 * No filtering
	 */
	NONE,

	/**
	 * Silently ignores the given element
	 */
	IGNORED,

	/**
	 * Raises an exception.
	 */
	FORBIDDEN;

	/**
	 * FORBIDDEN wins over IGNORED, IGNORED wins over NONE.
	 */
	public FilterBehavior merge(FilterBehavior other) {
		return this.ordinal() > other.ordinal() ? this : other;
	}
}
