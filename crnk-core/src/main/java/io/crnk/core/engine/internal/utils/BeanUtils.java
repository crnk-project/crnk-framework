package io.crnk.core.engine.internal.utils;

/**
 * Bean utils based on Crnk PropertyUtils
 */
public class BeanUtils {

	/**
	 * Get bean's property value and maps to String
	 *
	 * @param bean  bean to be accessed
	 * @param field bean's field
	 * @return bean's property value
	 * @see io.crnk.core.engine.internal.utils.PropertyUtils#getProperty(Object, String)
	 */
	public static String getProperty(Object bean, String field) {
		Object property = PropertyUtils.getProperty(bean, field);
		if (property == null) {
			return "null";
		}

		return property.toString();
	}
}
