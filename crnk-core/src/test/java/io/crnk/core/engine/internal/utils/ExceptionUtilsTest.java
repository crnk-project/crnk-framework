package io.crnk.core.engine.internal.utils;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;


public class ExceptionUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(ExceptionUtil.class);
	}

	@Test
	public void testNoError() {
		Assert.assertEquals(13, ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return 13;
			}
		}));
		Assert.assertEquals(13, ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return 13;
			}
		}, "test"));
	}


	@Test(expected = IllegalArgumentException.class)
	public void testRuntimeException() {
		ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				throw new IllegalArgumentException();
			}
		});
	}

	@Test(expected = IllegalStateException.class)
	public void testCheckedException() {
		ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				throw new IOException();
			}
		});
	}


	@Test
	public void testExceptionWithMessage() {
		try {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					throw new IllegalArgumentException();
				}
			}, "test %s", 13);
			Assert.fail();
		}
		catch (IllegalStateException e) {
			Assert.assertEquals("test 13", e.getMessage());
			Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
		}
	}

}
