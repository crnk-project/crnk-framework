package io.crnk.core.engine.result;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImmediateResultTest {

	private ImmediateResultFactory resultFactory = new ImmediateResultFactory();


	@Test
	public void checkNotAsync() {
		Assert.assertFalse(resultFactory.isAsync());

	}

	@Test
	public void checkContextAccess() throws ExecutionException, InterruptedException {
		Object context = new Object();
		Assert.assertFalse(resultFactory.hasThreadContext());
		resultFactory.setThreadContext(context);
		Assert.assertSame(context, resultFactory.getThreadContext());
		Assert.assertTrue(resultFactory.hasThreadContext());

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			Future<?> future = executorService.submit(new Runnable() {
				@Override
				public void run() {
					Assert.assertFalse(resultFactory.hasThreadContext());
				}
			});
			future.get();

			Assert.assertFalse(resultFactory.isAsync());
		} finally {
			executorService.shutdownNow();
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void subscribeNotSupported() {
		Result<Object> result = resultFactory.just(new Object());
		result.subscribe(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void onErrorResumeNotSupported() {
		Result<Object> result = resultFactory.just(new Object());
		result.onErrorResume(null);
	}
}
