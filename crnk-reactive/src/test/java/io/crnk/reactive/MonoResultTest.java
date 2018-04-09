package io.crnk.reactive;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.engine.result.Result;
import io.crnk.reactive.internal.MonoResult;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class MonoResultTest {

	@Test
	public void checkGet() {
		MonoResult<String> result = new MonoResult<>(Mono.just("foo"));
		Assert.assertEquals("foo", result.get());


		Flux<String> source = Flux.just("foo", "bar");

		StepVerifier.create(
				appendBoomError(source))
				.expectNext("foo")
				.expectNext("bar")
				.expectErrorMessage("boom")
				.verify();
	}

	public <T> Flux<T> appendBoomError(Flux<T> source) {
		return source.concatWith(Mono.error(new IllegalArgumentException("boom")));
	}

	@Test
	public void checkMap() {
		MonoResult<String> result = new MonoResult<>(Mono.just("foo"));
		Assert.assertEquals("FOO", result.map(it -> it.toUpperCase()).get());
	}

	@Test
	public void checkSubscribe() {
		List<String> items = new ArrayList<>();
		MonoResult<String> result = new MonoResult<>(Mono.just("foo"));
		result.subscribe(it -> items.add(it), it -> Assert.fail());
		Assert.assertEquals(1, items.size());
		Assert.assertEquals("foo", items.get(0));
	}

	@Test
	public void checkZip() {
		MonoResult<String> result1 = new MonoResult<>(Mono.just("foo1"));
		MonoResult<String> result2 = new MonoResult<>(Mono.just("foo2"));
		Result<String> zipped = result1.zipWith(result2, (it1, it2) -> it1 + "-" + it2);
		Assert.assertEquals("foo1-foo2", zipped.get());
	}

	@Test
	public void checkMerge() {
		MonoResult<String> result1 = new MonoResult<>(Mono.just("FOO"));
		Result<String> merged = result1.merge(it -> new MonoResult<>(Mono.just(it.toUpperCase())));
		Assert.assertEquals("FOO", merged.get());
	}
}
