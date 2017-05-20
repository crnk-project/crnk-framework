package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import io.crnk.core.engine.internal.utils.Predicate2;

public class JsonApiFieldPropertyFilter extends SimpleBeanPropertyFilter {

	private final Predicate2<Object, PropertyWriter> includeChecker;

	public JsonApiFieldPropertyFilter(Predicate2<Object, PropertyWriter> includeChecker) {
		this.includeChecker = includeChecker;
	}

	@Override
	public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer)
			throws Exception {
		if (include(bean, writer)) {
			super.serializeAsField(bean, jgen, provider, writer);
		}
	}

	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
			throws Exception {
		if (include(pojo, writer)) {
			super.serializeAsField(pojo, jgen, provider, writer);
		}
	}

	private boolean include(Object bean, PropertyWriter writer) {
		return includeChecker.test(bean, writer);
	}
}
