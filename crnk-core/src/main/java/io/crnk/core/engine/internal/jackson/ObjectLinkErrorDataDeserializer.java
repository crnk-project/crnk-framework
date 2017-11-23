package io.crnk.core.engine.internal.jackson;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author AdNovum Informatik AG
 */
public class ObjectLinkErrorDataDeserializer extends ErrorDataDeserializer {

	@Override
	String readAboutLink(JsonNode errorNode) throws IOException {
		JsonNode linksNode = errorNode.get(ErrorDataSerializer.LINKS);
		if (linksNode != null) {
			JsonNode aboutLinkNode = linksNode.get(ErrorDataSerializer.ABOUT_LINK);
			if (aboutLinkNode != null) {
				return readStringIfExists(LinksInformationSerializer.HREF, aboutLinkNode);
			}
		}
		return null;
	}

}