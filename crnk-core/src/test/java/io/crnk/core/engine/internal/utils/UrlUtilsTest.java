package io.crnk.core.engine.internal.utils;

import org.junit.Assert;
import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(UrlUtils.class);
	}

	@Test
	public void testRemoveTrailingSlash() {
		Assert.assertNull(UrlUtils.removeTrailingSlash(null));
		Assert.assertEquals("/test", UrlUtils.removeTrailingSlash("/test/"));
		Assert.assertEquals("test", UrlUtils.removeTrailingSlash("test/"));
		Assert.assertEquals("/test", UrlUtils.removeTrailingSlash("/test"));
		Assert.assertEquals("test", UrlUtils.removeTrailingSlash("test"));
	}

	@Test
	public void testRemoveLeadingSlash() {
		Assert.assertNull(UrlUtils.removeLeadingSlash(null));
		Assert.assertEquals("test/", UrlUtils.removeLeadingSlash("/test/"));
		Assert.assertEquals("test/", UrlUtils.removeLeadingSlash("test/"));
		Assert.assertEquals("test", UrlUtils.removeLeadingSlash("/test"));
		Assert.assertEquals("test", UrlUtils.removeLeadingSlash("test"));
	}
}
