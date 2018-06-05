package io.crnk.core.engine.parser;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

import io.crnk.core.engine.internal.utils.ExceptionUtil;

public class ConstructorBasedParser<T> implements StringParser<T> {

	private final Constructor constructor;

	public ConstructorBasedParser(Constructor constructor) {
		this.constructor = constructor;
	}

	@Override
	public T parse(String input) {

		return ExceptionUtil.wrapCatchedExceptions(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return (T) constructor.newInstance(input);
			}
		});

	}
}
