package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ExceptionMapperRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapperRegistry.class);

    private final List<ExceptionMapperType> exceptionMappers;

    ExceptionMapperRegistry(List<ExceptionMapperType> exceptionMappers) {
        this.exceptionMappers = exceptionMappers;
    }

    List<ExceptionMapperType> getExceptionMappers() {
        return exceptionMappers;
    }

    public Optional<ExceptionMapper> findMapperFor(Class<? extends Throwable> exceptionClass) {
        int currentDistance = Integer.MAX_VALUE;
        ExceptionMapper closestExceptionMapper = null;
        for (ExceptionMapperType mapperType : exceptionMappers) {
            int tempDistance = getDistanceBetweenExceptions(exceptionClass, mapperType.getExceptionClass());
            if (tempDistance < currentDistance) {
                currentDistance = tempDistance;
                closestExceptionMapper = mapperType.getExceptionMapper();
                if (currentDistance == 0) {
                    break;
                }
            }
        }
        return Optional.ofNullable(closestExceptionMapper);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <E extends Throwable> Optional<ExceptionMapper<E>> findMapperFor(ErrorResponse errorResponse) {
        int currentDepth = -1;
        ExceptionMapper closestExceptionMapper = null;

        for (ExceptionMapperType mapperType : exceptionMappers) {
            ExceptionMapper mapperObj = mapperType.getExceptionMapper();
            if (mapperObj instanceof ExceptionMapper) {
                ExceptionMapper mapper = (ExceptionMapper) mapperObj;
                boolean accepted = mapper.accepts(errorResponse);
                if (accepted) {
                    // the exception with the most super types is chosen
                    int tempDepth = countSuperTypes(mapperType.getExceptionClass());
                    if (tempDepth > currentDepth) {
                        currentDepth = tempDepth;
                        closestExceptionMapper = mapper;
                    }
                }
            }
        }
        return (Optional) Optional.ofNullable(closestExceptionMapper);
    }

    int getDistanceBetweenExceptions(Class<?> clazz, Class<?> mapperTypeClazz) {
        int distance = 0;
        Class<?> superClazz = clazz;

        if (!mapperTypeClazz.isAssignableFrom(clazz)) {
            return Integer.MAX_VALUE;
        }

        while (superClazz != mapperTypeClazz) {
            superClazz = superClazz.getSuperclass();
            distance++;
        }
        return distance;
    }

    int countSuperTypes(Class<?> clazz) {
        int count = 0;
        Class<?> superClazz = clazz;
        while (superClazz != Object.class) {
            superClazz = superClazz.getSuperclass();
            count++;
        }
        return count;
    }

    public Response toResponse(Throwable e) {
        Optional<ExceptionMapper> exceptionMapper = findMapperFor(e.getClass());
        if (!exceptionMapper.isPresent()) {
            LOGGER.error("failed to process operations request, unknown exception thrown", e);
            e = new InternalServerErrorException(e.getMessage());
            exceptionMapper = findMapperFor(e.getClass());
            PreconditionUtil
                    .assertTrue("no exception mapper for InternalServerErrorException found", exceptionMapper.isPresent());
        } else {
            LOGGER.debug("dispatching exception to mapper", e);
        }
        ErrorResponse errorResponse = exceptionMapper.get().toErrorResponse(e);
        return errorResponse.toResponse();
    }

    public Response toErrorResponse(Throwable e) {
        Optional<ExceptionMapper> exceptionMapper = findMapperFor(e.getClass());
        if (!exceptionMapper.isPresent()) {
            LOGGER.error("failed to process request, unknown exception thrown", e);

            // we do not propagate causes because we do not know the nature of the error.
            // one could consider hiding the message as well
            e = new InternalServerErrorException(e.getMessage());
            exceptionMapper = findMapperFor(e.getClass());
            PreconditionUtil
                    .assertTrue("no exception mapper for InternalServerErrorException found", exceptionMapper.isPresent());
        } else {
            LOGGER.debug("dispatching exception to mapper", e);
        }
        return exceptionMapper.get().toErrorResponse(e).toResponse();
    }
}