package io.crnk.core.engine.internal.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.utils.SerializerUtil;

/**
 * Serializes top-level Errors object.
 */
public class ErrorDataDeserializer extends JsonDeserializer<ErrorData> {

	@SuppressWarnings("unchecked")
	private static Map<String, Object> readMeta(JsonNode errorNode, JsonParser jp) throws IOException {
		JsonNode metaNode = errorNode.get(ErrorDataSerializer.META);
		if (metaNode != null) {
			return jp.getCodec().treeToValue(metaNode, Map.class);
		}
		return null;
	}

	private static String readSourcePointer(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorDataSerializer.SOURCE);
		if (node != null) {
			return SerializerUtil.readStringIfExists(ErrorDataSerializer.POINTER, node);
		}
		return null;
	}

	private static String readSourceParameter(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorDataSerializer.SOURCE);
		if (node != null) {
			return SerializerUtil.readStringIfExists(ErrorDataSerializer.PARAMETER, node);
		}
		return null;
	}

	private static String readAboutLink(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorDataSerializer.LINKS);
		if (node != null) {
			return SerializerUtil.deserializeLink(ErrorDataSerializer.ABOUT_LINK, node);
		}
		return null;
	}

	@Override
	public ErrorData deserialize(JsonParser jp, DeserializationContext context) throws IOException {
		JsonNode errorNode = jp.readValueAsTree();
		String id = SerializerUtil.readStringIfExists(ErrorDataSerializer.ID, errorNode);
		String aboutLink = readAboutLink(errorNode);
		String status = SerializerUtil.readStringIfExists(ErrorDataSerializer.STATUS, errorNode);
		String code = SerializerUtil.readStringIfExists(ErrorDataSerializer.CODE, errorNode);
		String title = SerializerUtil.readStringIfExists(ErrorDataSerializer.TITLE, errorNode);
		String detail = SerializerUtil.readStringIfExists(ErrorDataSerializer.DETAIL, errorNode);
		Map<String, Object> meta = readMeta(errorNode, jp);
		String sourcePointer = readSourcePointer(errorNode);
		String sourceParameter = readSourceParameter(errorNode);
		return new ErrorData(id, aboutLink, status, code, title, detail, sourcePointer, sourceParameter, meta);
	}

}
