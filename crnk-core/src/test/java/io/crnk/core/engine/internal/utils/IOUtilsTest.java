package io.crnk.core.engine.internal.utils;

import io.crnk.core.utils.CoreClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public class IOUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(IOUtils.class);
	}

	@Test
	public void readFully() throws IOException {
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			byte[] b = new byte[i * 100];
			r.nextBytes(b);
			Assert.assertTrue(Arrays.equals(b, IOUtils.readFully(new ByteArrayInputStream(b))));
		}
	}
}
