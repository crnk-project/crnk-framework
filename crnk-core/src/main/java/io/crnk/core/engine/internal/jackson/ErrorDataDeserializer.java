package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.engine.document.ErrorData;

import java.io.IOException;
import java.util.Map;

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
			return readStringIfExists(ErrorDataSerializer.POINTER, node);
		}
		return null;
	}

	private static String readSourceParameter(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorDataSerializer.SOURCE);
		if (node != null) {
			return readStringIfExists(ErrorDataSerializer.PARAMETER, node);
		}
		return null;
	}

	String readAboutLink(JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(ErrorDataSerializer.LINKS);
		if (node != null) {
			return readStringIfExists(ErrorDataSerializer.ABOUT_LINK, node);
		}
		return null;
	}

	static String readStringIfExists(String fieldName, JsonNode errorNode) throws IOException {
		JsonNode node = errorNode.get(fieldName);
		if (node != null) {
			return node.asText();
		} else {
			return null;
		}
	}

	@Override
	public ErrorData deserialize(JsonParser jp, DeserializationContext context) throws IOException {
		JsonNode errorNode = jp.readValueAsTree();
		String id = readStringIfExists(ErrorDataSerializer.ID, errorNode);
		String aboutLink = readAboutLink(errorNode);
		String status = readStringIfExists(ErrorDataSerializer.STATUS, errorNode);
		String code = readStringIfExists(ErrorDataSerializer.CODE, errorNode);
		String title = readStringIfExists(ErrorDataSerializer.TITLE, errorNode);
		String detail = readStringIfExists(ErrorDataSerializer.DETAIL, errorNode);
		Map<String, Object> meta = readMeta(errorNode, jp);
		String sourcePointer = readSourcePointer(errorNode);
		String sourceParameter = readSourceParameter(errorNode);
		return new ErrorData(id, aboutLink, status, code, title, detail, sourcePointer, sourceParameter, meta);
	}

}
