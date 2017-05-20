package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.crnk.core.utils.Nullable;

import java.io.IOException;

public class NullableSerializer extends JsonSerializer<Nullable<Object>> {

	@Override
	public void serialize(Nullable<Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value.isPresent()) {
			Object object = value.get();
			if (object == null) {
				gen.writeNull();
			} else {
				gen.writeObject(object);
			}
		}
	}

	@Override
	public boolean isEmpty(SerializerProvider provider, Nullable<Object> value) {
		return !value.isPresent();
	}
}
