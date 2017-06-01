package io.crnk.meta;

import io.crnk.meta.internal.MetaUtils;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class MetaUtilsTest {

	@Test
	public void noPublicDefaultConstrcutor() {
		ClassTestUtils.assertPrivateConstructor(MetaUtils.class);
	}

	@Test
	public void testFirstToLower() {
		Assert.assertEquals("test", MetaUtils.firstToLower("test"));
		Assert.assertEquals("test", MetaUtils.firstToLower("Test"));
		Assert.assertEquals("", MetaUtils.firstToLower(""));
	}
}
