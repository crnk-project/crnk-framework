package io.crnk.core.engine.parser;

import io.crnk.core.engine.internal.utils.ExceptionUtil;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class MethodBasedParser<T> implements StringParser<T> {

	private final Method method;

	private final Class clazz;

	public MethodBasedParser(Method method, Class clazz) {
		this.method = method;
		this.clazz = clazz;
	}

	@Override
	public T parse(String input) {

		return ExceptionUtil.wrapCatchedExceptions(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return (T) method.invoke(clazz, input);
			}
		});

	}
}
