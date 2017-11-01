package io.crnk.brave;

import io.crnk.brave.internal.BraveUtil;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Test;

public class BraveUtilTest {


	@Test
	public void testHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(BraveUtil.class);
	}
}
