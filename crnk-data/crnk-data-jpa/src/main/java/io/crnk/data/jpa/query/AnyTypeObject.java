package io.crnk.data.jpa.query;

public interface AnyTypeObject {

	/**
	 * @return the name of the type of the currently set attribute.
	 */
	String getType();

	/**
	 * @return the value of this anytype.
	 */
	Object getValue();

	/**
	 * Sets the value of this anytype
	 *
	 * @param value the new value
	 */
	void setValue(Object value);

	/**
	 * @param <T>   value type
	 * @param clazz to cast the value to
	 * @return the value of this anytype cast to the desired class.
	 */
	<T> T getValue(Class<T> clazz);

}
