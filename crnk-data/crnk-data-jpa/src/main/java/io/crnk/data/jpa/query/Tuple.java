package io.crnk.data.jpa.query;

public interface Tuple {

	<T> T get(String name, Class<T> clazz);

	<T> T get(int index, Class<T> clazz);

	/**
	 * Ignores the given number of entries by incrementing any index access accordingly.
	 *
	 * @param numEntriesToIgnore for this tuple
	 */
	void reduce(int numEntriesToIgnore);
}
