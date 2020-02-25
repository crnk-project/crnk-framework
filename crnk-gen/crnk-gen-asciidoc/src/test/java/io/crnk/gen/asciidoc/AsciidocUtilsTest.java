package io.crnk.gen.asciidoc;

import io.crnk.gen.asciidoc.internal.AsciidocUtils;
import org.junit.Assert;
import org.junit.Test;

public class AsciidocUtilsTest {

	@Test
	public void testHrefRewrite() {
		Assert.assertEquals("Test http://www.google.com[Google]", AsciidocUtils.fromHtml("Test <a href=\"http://www.google.com\">Google</a>"));
	}
}
