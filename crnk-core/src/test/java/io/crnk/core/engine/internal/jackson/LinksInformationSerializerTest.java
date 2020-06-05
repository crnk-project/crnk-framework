package io.crnk.core.engine.internal.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.links.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LinksInformationSerializerTest {

	private static final String LINK = "\"%s\":\"%s\"";
	private static final String OBJECT_LINK = "\"%s\":{\"href\":\"%s\"}";

	private TestSelfLinksInformation selfLink;
	private DefaultPagedLinksInformation pagedLink;
	private TestCustomLinksInformation customLink;
	private TestCustomStringBasedLinksInformation customStringLink;

	@Before
	public void setup() {
		selfLink = new TestSelfLinksInformation("/self");

		pagedLink = new DefaultPagedLinksInformation();
		pagedLink.setFirst("/first");
		pagedLink.setLast("/last");
		// not setting previous -> first page
		pagedLink.setNext("/next");

		customLink = new TestCustomLinksInformation("http://www.imdb.com");
		customStringLink = new TestCustomStringBasedLinksInformation("http://www.imdb.com");
	}

	/*@Test
	public void testSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule());

		String serialized = mapper.writeValueAsString(selfLink);
		String expected = createSingleLinkJson(LINK, "self", selfLink.getSelf().getHref());
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(pagedLink);
		expected = createMultiLinkJson(LINK,
				Arrays.asList("first", "last", "next"),
				Arrays.asList(pagedLink.getFirst().getHref(), pagedLink.getLast().getHref(), pagedLink.getNext().getHref()));
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(customLink);
		expected = createSingleLinkJson(LINK, "imdb", customLink.getImdb().getHref());
		Assert.assertEquals(expected, serialized);
	}*/

	@Test
	public void testObjectLinkSerialization() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule(true));

		String serialized = mapper.writeValueAsString(selfLink);
		String expected = createSingleLinkJson(OBJECT_LINK, "self", selfLink.getSelf().getHref());
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(pagedLink);
		// methods are not ordered when retrieved via reflection -> check individual links
		Assert.assertTrue(serialized.contains(createSingleLinkJson(OBJECT_LINK, "first", pagedLink.getFirst().getHref(), false)));
		Assert.assertTrue(serialized.contains(createSingleLinkJson(OBJECT_LINK, "last", pagedLink.getLast().getHref(), false)));
		Assert.assertTrue(serialized.contains(createSingleLinkJson(OBJECT_LINK, "next", pagedLink.getNext().getHref(), false)));

		serialized = mapper.writeValueAsString(customLink);
		expected = createMultiLinkJson(OBJECT_LINK, Arrays.asList("imdb", "another-imdb"), Arrays.asList(customLink.getImdb().getHref(), customLink.getAnotherImdb().getHref()));
		Assert.assertEquals(expected, serialized);

		serialized = mapper.writeValueAsString(customStringLink);
		expected = createMultiLinkJson(OBJECT_LINK, Arrays.asList("imdb", "another_imdb"), Arrays.asList(customStringLink.getImdb(), customStringLink.getAnotherImdb()));
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

		private Link self;

		TestSelfLinksInformation(String self) {
			this.self = new DefaultLink(self);
		}

		@Override
		public Link getSelf() {
			return self;
		}

		@Override
		public void setSelf(Link self) {

		}
	}

	public static class TestCustomLinksInformation implements LinksInformation {

		private Link imdb;

		@JsonProperty("another-imdb")
		private Link anotherImdb;

		TestCustomLinksInformation(String imdb) {
			this.imdb = new DefaultLink(imdb);
			this.anotherImdb = new DefaultLink(imdb);
		}

		public Link getImdb() {
			return imdb;
		}

		public Link getAnotherImdb() {
			return anotherImdb;
		}
	}

	public static class TestCustomStringBasedLinksInformation implements LinksInformation {

		private String imdb;

		@JsonProperty("another_imdb")
		private String anotherImdb;

		TestCustomStringBasedLinksInformation(String imdb) {
			this.imdb = imdb;
			this.anotherImdb = imdb;
		}

		public String getImdb() {
			return imdb;
		}

		public String getAnotherImdb() {
			return anotherImdb;
		}
	}

}