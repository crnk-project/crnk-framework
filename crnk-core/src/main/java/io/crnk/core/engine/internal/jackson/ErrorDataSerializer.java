package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperUtil;
import io.crnk.core.engine.internal.utils.SerializerUtil;

import java.io.IOException;

/**
 * Serializes top-level Errors object.
 */
public class ErrorDataSerializer extends JsonSerializer<ErrorData> {

	public static final String LINKS = "links";
	public static final String ID = "id";
	public static final String ABOUT_LINK = "about";
	public static final String STATUS = "status";
	public static final String CODE = "code";
	public static final String TITLE = "title";
	public static final String DETAIL = "detail";
	public static final String SOURCE = "source";
	public static final String POINTER = "pointer";
	public static final String PARAMETER = "parameter";
	public static final String META = "meta";

	private static void writeMeta(ErrorData errorData, JsonGenerator gen) throws IOException {
		if (errorData.getMeta() != null) {
			gen.writeObjectField(META, errorData.getMeta());
		}
	}

	private static void writeSource(ErrorData errorData, JsonGenerator gen) throws IOException {
		if (errorData.getSourceParameter() != null || errorData.getSourcePointer() != null) {
			gen.writeObjectFieldStart(SOURCE);
			SerializerUtil.writeStringIfExists(POINTER, errorData.getSourcePointer(), gen);
			SerializerUtil.writeStringIfExists(PARAMETER, errorData.getSourceParameter(), gen);
			gen.writeEndObject();
		}
	}

	private static void writeAboutLink(ErrorData errorData, JsonGenerator gen) throws IOException {
		if (errorData.getAboutLink() != null) {
			SerializerUtil serializerUtil = DocumentMapperUtil.getSerializerUtil();

			gen.writeObjectFieldStart(LINKS);
			serializerUtil.serializeLink(gen, ABOUT_LINK, errorData.getAboutLink());
			gen.writeEndObject();
		}
	}

	@Override
	public void serialize(ErrorData errorData, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {

		gen.writeStartObject();
		SerializerUtil.writeStringIfExists(ID, errorData.getId(), gen);
		writeAboutLink(errorData, gen);
		SerializerUtil.writeStringIfExists(STATUS, errorData.getStatus(), gen);
		SerializerUtil.writeStringIfExists(CODE, errorData.getCode(), gen);
		SerializerUtil.writeStringIfExists(TITLE, errorData.getTitle(), gen);
		SerializerUtil.writeStringIfExists(DETAIL, errorData.getDetail(), gen);
		writeSource(errorData, gen);
		writeMeta(errorData, gen);
		gen.writeEndObject();
	}

	@Override
	public Class<ErrorData> handledType() {
		return ErrorData.class;
	}

}
