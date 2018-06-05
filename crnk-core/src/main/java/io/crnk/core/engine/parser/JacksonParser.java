package io.crnk.core.engine.parser;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.TextNode;

public class JacksonParser<T> implements StringParser<T> {

	private final ObjectReader reader;

	public JacksonParser(ObjectReader reader) {
		this.reader = reader;
	}

	@Override
	public T parse(String input) {
		JsonNode node = new TextNode(input);
		try {
			return reader.readValue(node);
		}
		catch (IOException e) {
			throw new ParserException("Cannot parse " + input, e);
		}
	}
}
