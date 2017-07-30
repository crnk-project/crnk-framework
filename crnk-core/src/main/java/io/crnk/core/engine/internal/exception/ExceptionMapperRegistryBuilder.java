package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.legacy.internal.DefaultExceptionMapperLookup;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class ExceptionMapperRegistryBuilder {
	private final Set<ExceptionMapperType> exceptionMappers = new HashSet<>();

	public ExceptionMapperRegistry build(String resourceSearchPackage) {
		return build(new DefaultExceptionMapperLookup(resourceSearchPackage));
	}

	public ExceptionMapperRegistry build(ExceptionMapperLookup exceptionMapperLookup) {
		addDefaultMappers();
		for (JsonApiExceptionMapper<?> exceptionMapper : exceptionMapperLookup.getExceptionMappers()) {
			registerExceptionMapper(exceptionMapper);
		}
		return new ExceptionMapperRegistry(exceptionMappers);
	}

	private void addDefaultMappers() {
		registerExceptionMapper(new CrnkExceptionMapper());
	}

	private void registerExceptionMapper(JsonApiExceptionMapper<? extends Throwable> exceptionMapper) {
		Class<? extends JsonApiExceptionMapper> mapperClass = exceptionMapper.getClass();
		Class<? extends Throwable> exceptionClass = getGenericType(mapperClass);
		if (exceptionClass == null && isProxy(mapperClass)) {
			// deal if dynamic proxies, like in CDI
			mapperClass = (Class<? extends JsonApiExceptionMapper>) mapperClass.getSuperclass();
			exceptionClass = getGenericType(mapperClass);
		}

		exceptionMappers.add(new ExceptionMapperType(exceptionClass, exceptionMapper));
	}

	private boolean isProxy(Class<? extends JsonApiExceptionMapper> mapperClass) {
		return mapperClass.getName().contains("$$")
				&& JsonApiExceptionMapper.class.isAssignableFrom(mapperClass.getSuperclass());
	}

	private Class<? extends Throwable> getGenericType(Class<? extends JsonApiExceptionMapper> mapper) {
		Type[] types = mapper.getGenericInterfaces();
		if (null == types || 0 == types.length) {
			types = new Type[]{mapper.getGenericSuperclass()};
		}

		for (Type type : types) {
			Class<?> rawType = ClassUtils.getRawType(type);
			if (type instanceof ParameterizedType && JsonApiExceptionMapper.class.isAssignableFrom(rawType))
			{
				//noinspection unchecked
				return (Class<? extends Throwable>) ((ParameterizedType) type).getActualTypeArguments()[0];
			}
		}

		if(isProxy(mapper)){
			return getGenericType((Class<? extends JsonApiExceptionMapper>) mapper.getSuperclass());
		}

		//Won't get in here
		throw new IllegalStateException("unable to discover exception class for " + mapper.getName());
	}

}
