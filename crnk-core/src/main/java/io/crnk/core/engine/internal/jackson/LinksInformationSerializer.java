package io.crnk.core.engine.internal.jackson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.resource.links.LinksInformation;

/**
 * Serializes {@link LinksInformation} objects as JSON objects instead of simple JSON attributes.
 */
public class LinksInformationSerializer extends JsonSerializer<LinksInformation> {

	@Override
	public void serialize(LinksInformation value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {

		gen.writeStartObject();
		Method[] methods = value.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (usableGetter(method)) {
				String link = getValue(value, method);
				String fieldName = getFieldName(method);
				writeObjectLink(fieldName, link, gen);
			}
		}
		gen.writeEndObject();

	}

	private boolean usableGetter(Method method) {
		boolean isGetter = method.getName().startsWith("get");
		boolean returnsString = String.class.equals(method.getReturnType());
		return isGetter && returnsString;
	}

	private String getValue(Object object, Method method) {
		try {
			if (method.isAnnotationPresent(JsonIgnore.class)) {
				return null;
			}
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			Object value = method.invoke(object);
			return (String) value;
		}
		catch (IllegalAccessException e) {
			// ignore
		}
		catch (InvocationTargetException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Remove "get" from the method name and make sure the first letter is lower case
	 * @param method the method for which to return the corresponding field name
	 * @return lower case field name based on the given method
	 */
	private String getFieldName(Method method) {
		String name = method.getName();
		return name.substring(3, 4).toLowerCase() + name.substring(4);
	}

	private void writeObjectLink(String fieldName, String value, JsonGenerator gen) throws IOException {
		if (value != null) {
			gen.writeObjectFieldStart(fieldName);
			gen.writeStringField(SerializerUtil.HREF, value);
			gen.writeEndObject();
		}
	}

	@Override
	public Class<LinksInformation> handledType() {
		return LinksInformation.class;
	}

}
