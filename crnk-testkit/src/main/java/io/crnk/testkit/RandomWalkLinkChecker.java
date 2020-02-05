package io.crnk.testkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.utils.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a bounded random walk across an API endpoint by following JSON:API links.
 */
public class RandomWalkLinkChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkLinkChecker.class);

	private final HttpAdapter httpAdapter;

	private int walkLength = 1000;

	private Random random = new Random();

	private Set<String> visited = new HashSet<>();

	private List<String> upcoming = new ArrayList<>();

	private ObjectMapper mapper = new ObjectMapper();

	private Map<String, String> urlToSourceMapping = new HashMap<>();

	private String currentUrl;

	private List<Predicate<String>> blackListPredicates = new ArrayList<>();

	private List<String> previousVisits = new ArrayList<>();

	private int maxPreviousVisitHistorySize = 30;

	public RandomWalkLinkChecker(HttpAdapter httpAdapter) {
		this.httpAdapter = httpAdapter;
	}

	/**
	 * @param seed for randomness to gain determinism.
	 */
	public void setSeed(long seed) {
		random.setSeed(seed);
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

	private String greatestCommonPrefix(String a, String b) {
		int minLength = Math.min(a.length(), b.length());
		for (int i = 0; i < minLength; i++) {
			if (a.charAt(i) != b.charAt(i)) {
				return a.substring(0, i);
			}
		}
		return a.substring(0, minLength);
	}

	public Set<String> performCheck() {


		int index = 0;
		while (index < walkLength && !upcoming.isEmpty()) {

			int nextIndex = selectNext();

			currentUrl = upcoming.remove(nextIndex);
			if (!visited.contains(currentUrl)) {
				visited.add(currentUrl);
				index++;
				visit(currentUrl);

				previousVisits.add(currentUrl);
				while (previousVisits.size() > maxPreviousVisitHistorySize) {
					previousVisits.remove(0);
				}
			}
		}
		return visited;
	}

	/**
	 * @return select next url to visit from the upcoming queue. We attempt to choose an url that is most different to any other previously visited urls.
	 */
	protected int selectNext() {
		// consider moving to a prefix tree to better distribute testing evenly across all repositories
		int numCandidates = 100;
		int nextIndex = -1;
		int nextPriority = Integer.MAX_VALUE;
		for (int i = 0; i < numCandidates; i++) {
			int candidateIndex = random.nextInt(upcoming.size());
			String candidateUrl = upcoming.get(candidateIndex);
			int candidatePriority = 0;
			for (String previousVisit : previousVisits) {
				candidatePriority += greatestCommonPrefix(previousVisit, candidateUrl).length();
			}
			if (candidatePriority < nextPriority) {
				nextIndex = candidateIndex;
				nextPriority = candidatePriority;
			}
		}
		return nextIndex;
	}

	protected void visit(String url) {
		try {
			LOGGER.info("visiting {}", url);
			HttpAdapterRequest request = httpAdapter.newRequest(url, HttpMethod.GET, null);
			HttpAdapterResponse response = request.execute();
			int code = response.code();
			if (!accept(url, response)) {
				throw new IllegalStateException("expected endpoint to return success status code, got " + code + " from " + url + ", url obtained from " + urlToSourceMapping.get(url));
			}
			String body = response.body();
			if (body == null) {
				throw new IllegalStateException("expected a body to be returned from  " + url + ", url obtained from " + urlToSourceMapping.get(url));
			}

			JsonNode jsonNode = mapper.reader().readTree(body);
			findLinks(jsonNode);
		} catch (IOException e) {
			throw new IllegalStateException("failed to visit " + url + ", url obtained from " + urlToSourceMapping.get(url), e);
		}
	}

	/**
	 * @param url
	 * @param response
	 * @return true if the given response is acceptable
	 */
	protected boolean accept(String url, HttpAdapterResponse response) {
		int code = response.code();
		return code < 300;
	}

	protected void findLinks(JsonNode jsonNode) {
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
					if (!(linksValue instanceof ObjectNode)) {
						throw new IllegalStateException("illegal use of links field in " + currentUrl + ": " + linksValue);
					}
					collectLinks((ObjectNode) linksValue);
				} else {
					findLinks(entry.getValue());
				}
			}
		}
	}

	protected void collectLinks(ObjectNode linksNode) {
		Iterator<Map.Entry<String, JsonNode>> iterator = linksNode.fields();
		while (iterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = iterator.next();
			JsonNode link = entry.getValue();
			String url = link.asText();
			if (url != null && url.startsWith("http") && !visited.contains(url)) {
				queueLink(url);
			}
		}
	}

	public void addBlackListPredicate(Predicate<String> blackListPredicate) {
		this.blackListPredicates.add(blackListPredicate);
	}

	protected void queueLink(String url) {
		boolean blacklisted = blackListPredicates.stream().anyMatch(it -> it.test(url));
		if (!blacklisted) {
			upcoming.add(url);
			if (!urlToSourceMapping.containsKey(url)) {
				urlToSourceMapping.put(url, currentUrl);
			}
		}
	}
}
