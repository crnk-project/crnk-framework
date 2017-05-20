package io.crnk.client.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.internal.utils.CastableInformation;
import io.crnk.core.resource.links.LinksInformation;

import java.io.IOException;

public class JsonLinksInformation implements LinksInformation, CastableInformation<LinksInformation> {

	private JsonNode data;

	private ObjectMapper mapper;

	public JsonLinksInformation(JsonNode data, ObjectMapper mapper) {
		this.data = data;
		this.mapper = mapper;
	}

	public JsonNode asJsonNode() {
		return data;
	}

	/**
	 * Converts this generic links information to the provided type.
	 *
	 * @param linksClass to return
	 * @return links information based on the provided type.
	 */
	@Override
	public <L extends LinksInformation> L as(Class<L> linksClass) {
		try {
			return mapper.readerFor(linksClass).readValue(data);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
