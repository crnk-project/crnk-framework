package io.crnk.servlet.reactive;

import io.crnk.servlet.internal.AsyncAdapter;
import org.junit.Test;

import java.io.IOException;

public class AsyncAdapterTest {

	@Test
	public void doesNothing() throws IOException {
		AsyncAdapter adapter = new AsyncAdapter();
		adapter.onTimeout(null);
		adapter.onComplete(null);
		adapter.onError(null);
		adapter.onStartAsync(null);
	}
}
