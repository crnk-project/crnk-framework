package io.crnk.core.engine.internal.jackson;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.utils.Nullable;

public class DocumentDataDeserializer extends JsonDeserializer<Nullable<Object>> {

	@Override
	public Nullable<Object> deserialize(JsonParser jp, DeserializationContext context) throws IOException {
		JsonToken currentToken = jp.getCurrentToken();
		if (currentToken == JsonToken.START_ARRAY) {
			Resource[] resources = jp.readValueAs(Resource[].class);
			return Nullable.of((Object) Arrays.asList(resources));
		} else if (currentToken == JsonToken.VALUE_NULL) {
			return Nullable.nullValue();
		} else {
			PreconditionUtil.verifyEquals(currentToken, JsonToken.START_OBJECT, "parsing failed");
			return Nullable.of((Object) jp.readValueAs(Resource.class));
		}
	}

	@Override
	public Nullable<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
		return Nullable.nullValue();
	}
}
