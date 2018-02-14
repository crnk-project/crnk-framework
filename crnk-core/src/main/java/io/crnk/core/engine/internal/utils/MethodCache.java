package io.crnk.core.engine.internal.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MethodCache {

	private Map<MethodCacheKey, Optional<Method>> cache = new ConcurrentHashMap<>();

	public Optional<Method> find(Class<?> clazz, String name, Class<?>... parameters) {
		MethodCacheKey entry = new MethodCacheKey(clazz, name, parameters);
		Optional<Method> method = cache.get(entry);
		if (method == null) {
			try {
				method = Optional.of(clazz.getMethod(name, parameters));
			}
			catch (NoSuchMethodException e) { // NOSONAR
				method = Optional.empty();
			}
			cache.put(entry, method);
		}
		return method;
	}

	protected static class MethodCacheKey {

		private final Class<?> clazz;

		private final String name;

		@SuppressWarnings("rawtypes")
		private final Class[] parameters;

		public MethodCacheKey(Class<?> clazz, String name, Class<?>[] parameters) {
			this.clazz = clazz;
			this.name = name;
			this.parameters = parameters;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + Arrays.hashCode(parameters);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != MethodCacheKey.class) {
				return false;
			}
			MethodCacheKey other = (MethodCacheKey) obj;
			return CompareUtils.isEquals(clazz, other.clazz) && CompareUtils.isEquals(name, other.name) && Arrays
					.equals(parameters, other.parameters);
		}
	}
}
