package io.crnk.core.utils;

/**
 * Priority for registered objects like filters and request processors.
 * <p>
 * Objects not implementing Prioritizable have priority 0. If two objects have the same priority, the original registration order is preserved.
 */
public interface Prioritizable {

	/**
	 * The higher the returned value, the later it will be used.
	 * e.g. 1 as first priority, 2 as second priority, etc.
	 */
	int getPriority();
}
