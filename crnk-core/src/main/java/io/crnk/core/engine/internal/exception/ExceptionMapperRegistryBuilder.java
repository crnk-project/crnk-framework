package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.utils.Prioritizable;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class ExceptionMapperRegistryBuilder {
    private List<ExceptionMapperType> exceptionMappers = new ArrayList<>();

    public ExceptionMapperRegistry build(ExceptionMapperLookup exceptionMapperLookup) {
        for (ExceptionMapper<?> exceptionMapper : exceptionMapperLookup.getExceptionMappers()) {
            registerExceptionMapper(exceptionMapper);
        }
        exceptionMappers = Prioritizable.prioritze(exceptionMappers);
        addDefaultMappers();
        return new ExceptionMapperRegistry(exceptionMappers);
    }

    private void addDefaultMappers() {
        registerExceptionMapper(new CrnkExceptionMapper());
        registerExceptionMapper(new TimeoutExceptionMapper());
    }

    private void registerExceptionMapper(ExceptionMapper<? extends Throwable> exceptionMapper) {
        Class<? extends ExceptionMapper> mapperClass = exceptionMapper.getClass();
        Class<? extends Throwable> exceptionClass = getGenericType(mapperClass);
        if (exceptionClass == null && isProxy(mapperClass)) {
            // deal if dynamic proxies, like in CDI
            mapperClass = (Class<? extends ExceptionMapper>) mapperClass.getSuperclass();
            exceptionClass = getGenericType(mapperClass);
        }

        exceptionMappers.add(new ExceptionMapperType(exceptionClass, exceptionMapper));
    }

    private boolean isProxy(Class<? extends ExceptionMapper> mapperClass) {
        return mapperClass.getName().contains("$$")
                && ExceptionMapper.class.isAssignableFrom(mapperClass.getSuperclass());
    }

    private Class<? extends Throwable> getGenericType(Class<? extends ExceptionMapper> mapper) {
        try {
            Type[] types = mapper.getGenericInterfaces();
            if (null == types || 0 == types.length) {
                types = new Type[]{mapper.getGenericSuperclass()};
            }

            for (Type type : types) {
                Class<?> rawType = ClassUtils.getRawType(type);
                if (type instanceof ParameterizedType && ExceptionMapper.class.isAssignableFrom(rawType)) {
                    //noinspection unchecked
                    return (Class<? extends Throwable>) ((ParameterizedType) type).getActualTypeArguments()[0];
                }
            }

            if (isProxy(mapper)) {
                return getGenericType((Class<? extends ExceptionMapper>) mapper.getSuperclass());
            }

            //Won't get in here
            throw new IllegalStateException("unable to discover exception class for " + mapper.getName());
        } catch (MalformedParameterizedTypeException e) {
            throw new IllegalStateException(mapper.getName(), e);
        }
    }

}
