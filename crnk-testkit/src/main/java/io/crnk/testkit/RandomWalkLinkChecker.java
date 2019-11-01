package io.crnk.testkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a random walk across an API endpoint by following JSON:API links.
 */
public class RandomWalkLinkChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkLinkChecker.class);

	private final HttpAdapter httpAdapter;

	private int walkLength = 1000;

	private Set<String> visited = new HashSet<>();

	private List<String> upcoming = new ArrayList<>();

	private ObjectMapper mapper = new ObjectMapper();

	private String currentUrl;

	public RandomWalkLinkChecker(HttpAdapter httpAdapter) {
		this.httpAdapter = httpAdapter;
	}

	public void addStartUrl(String url) {
		upcoming.add(url);
	}

	public void setWalkLength(int walkLength) {
		this.walkLength = walkLength;
	}

	public int getWalkLength() {
		return walkLength;
	}

	public Set<String> performCheck() {
		Random random = new Random();

		int index = 0;
		while (index < walkLength && !upcoming.isEmpty()) {

			int nextIndex = random.nextInt(upcoming.size());

			currentUrl = upcoming.remove(nextIndex);
			if (!visited.contains(currentUrl)) {
				visited.add(currentUrl);
				index++;
				visit(currentUrl);
			}
		}
		return visited;
	}

	private void visit(String url) {
		try {
			LOGGER.info("visiting {}", url);
			HttpAdapterRequest request = httpAdapter.newRequest(url, HttpMethod.GET, null);
			HttpAdapterResponse response = request.execute();
			int code = response.code();
			if (code >= 300) {
				throw new IllegalStateException("expected endpoint to return success status code, got " + code + " from " + url);
			}
			String body = response.body();
			if (body == null) {
				throw new IllegalStateException("expected a body to be returned from  " + url);
			}

			JsonNode jsonNode = mapper.reader().readTree(body);
			findLinks(jsonNode);
		} catch (IOException e) {
			throw new IllegalStateException("failed to visit " + url, e);
		}
	}

	private void findLinks(JsonNode jsonNode) {
		if (jsonNode instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				JsonNode childNode = arrayNode.get(i);
				findLinks(childNode);
			}
		} else if (jsonNode instanceof ObjectNode) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> entry = fields.next();
				String name = entry.getKey();
				if (name.equals("links")) {
					JsonNode linksValue = entry.getValue();
					if(!(linksValue instanceof  ObjectNode)){
						throw new IllegalStateException("illegal use of links field in " + currentUrl + ": " + linksValue);
					}
					collectLinks((ObjectNode) linksValue);
				} else {
					findLinks(entry.getValue());
				}
			}
		}
	}

	private void collectLinks(ObjectNode linksNode) {
		Iterator<Map.Entry<String, JsonNode>> iterator = linksNode.fields();
		while (iterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = iterator.next();
			JsonNode link = entry.getValue();
			String url = link.asText();
			if (url == null || !url.startsWith("http")) {
				try {
					String linkName = entry.getKey();
					throw new IllegalStateException(
							"expected link `" + linkName + "` from " + currentUrl + " to contain a valid link, got " + url + " from " + mapper.writer().writeValueAsString(linksNode));
				} catch (JsonProcessingException e) {
					throw new IllegalStateException(e);
				}
			} else if (!visited.contains(url)) {
				upcoming.add(url);
			}
		}
	}
}
