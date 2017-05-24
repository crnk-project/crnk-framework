package io.crnk.core.engine.internal.utils;

import org.junit.Assert;
import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void testRemoveTrailingSlash() {
		Assert.assertEquals("/test", UrlUtils.removeTrailingSlash("/test/"));
		Assert.assertEquals("test", UrlUtils.removeTrailingSlash("test/"));
		Assert.assertEquals("/test", UrlUtils.removeTrailingSlash("/test"));
		Assert.assertEquals("test", UrlUtils.removeTrailingSlash("test"));
	}
}
