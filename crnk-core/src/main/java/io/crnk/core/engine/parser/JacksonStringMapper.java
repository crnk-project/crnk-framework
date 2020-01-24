package io.crnk.core.engine.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

public class JacksonStringMapper<T> implements StringMapper<T> {

	private final ObjectReader reader;

	private final ObjectMapper mapper;


	public JacksonStringMapper(ObjectMapper mapper, Class clazz) {
		this.reader = mapper.readerFor(clazz);
		this.mapper = mapper;
	}

	@Override
	public T parse(String input) {
		JsonNode node = new TextNode(input);
		try {
			return reader.readValue(node);
		} catch (IOException e) {
			throw new ParserException("Cannot parse " + input, e);
		}
	}

	@Override
	public String toString(T input) {
		JsonNode jsonNode = mapper.valueToTree(input);

		if (jsonNode instanceof TextNode) {
			return jsonNode.textValue();
		}
		if (jsonNode instanceof NumericNode) {
			return jsonNode.asText();
		}

		// fallback to String for complex type
		return input.toString();
	}
}
