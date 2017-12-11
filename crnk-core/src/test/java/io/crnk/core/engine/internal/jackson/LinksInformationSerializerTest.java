package io.crnk.core.engine.internal.jackson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LinksInformationSerializerTest {

	private static final String LINK = "\"%s\":\"%s\"";
	private static final String OBJECT_LINK = "\"%s\":{\"href\":\"%s\"}";

	private TestSelfLinksInformation selfLink;
	private DefaultPagedLinksInformation pagedLink;
	private TestCustomLinksInformation customLink;

	@Before
	public void setup() {
		selfLink = new TestSelfLinksInformation("/self");

		pagedLink = new DefaultPagedLinksInformation();
		pagedLink.setFirst("/first");
		pagedLink.setLast("/last");
		// not setting previous -> first page
		pagedLink.setNext("/next");

		customLink = new TestCustomLinksInformation("http://www.imdb.com");
	}

	@Test
	public void testSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule());

		String serialized = mapper.writeValueAsString(selfLink);
		String expected = createSingleLinkJson(LINK, "self", selfLink.getSelf());
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(pagedLink);
		expected = createMultiLinkJson(LINK,
				Arrays.asList("first", "last", "next"),
				Arrays.asList(pagedLink.getFirst(), pagedLink.getLast(), pagedLink.getNext()));
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(customLink);
		expected = createSingleLinkJson(LINK, "imdb", customLink.getImdb());
		Assert.assertEquals(expected, serialized);
	}

	@Test
	public void testObjectLinkSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule(true));

		String serialized = mapper.writeValueAsString(selfLink);
		String expected = createSingleLinkJson(OBJECT_LINK, "self", selfLink.getSelf());
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(pagedLink);
		// methods are not ordered when retrieved via reflection -> check individual links
		Assert.assertTrue(serialized.contains(createSingleLinkJson(OBJECT_LINK, "first", pagedLink.getFirst(), false)));
		Assert.assertTrue(serialized.contains(createSingleLinkJson(OBJECT_LINK, "last", pagedLink.getLast(), false)));
		Assert.assertTrue(serialized.contains(createSingleLinkJson(OBJECT_LINK, "next", pagedLink.getNext(), false)));

		serialized = mapper.writeValueAsString(customLink);
		expected = createSingleLinkJson(OBJECT_LINK, "imdb", customLink.getImdb());
		Assert.assertEquals(expected, serialized);
	}

	private String createSingleLinkJson(String template, String title, String url) {
		return createSingleLinkJson(template, title, url, true);
	}

	private String createSingleLinkJson(String template, String title, String url, boolean includeStartEndBraces) {
		return (includeStartEndBraces ? "{" : "") + String.format(template, title, url) + (includeStartEndBraces ? "}" : "");
	}

	private String createMultiLinkJson(String template, List<String> titles, List<String> urls) {
		int numLinks = titles.size();
		StringBuilder links = new StringBuilder();
		for (int i = 0; i < numLinks; i++) {
			if (i > 0) {
				links.append(",");
			}
			links.append(String.format(template, titles.get(i), urls.get(i)));
		}
		return "{" + links.toString() + "}";
	}

	public static class TestSelfLinksInformation implements SelfLinksInformation {

		private String self;

		TestSelfLinksInformation(String self) {
			this.self = self;
		}

		@Override
		public String getSelf() {
			return self;
		}

		@Override
		public void setSelf(String self) {

		}
	}

	public static class TestCustomLinksInformation implements LinksInformation {

		private String imdb;

		TestCustomLinksInformation(String imdb) {
			this.imdb = imdb;
		}

		public String getImdb() {
			return imdb;
		}
	}

}