package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.utils.Prioritizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ExceptionMapperRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapperRegistry.class);

    private final List<ExceptionMapperType> exceptionMappers;
    private final boolean orderExceptionMapperAfterClass;

	ExceptionMapperRegistry(List<ExceptionMapperType> exceptionMappers) {
		this( exceptionMappers, true);
	}

	ExceptionMapperRegistry(List<ExceptionMapperType> exceptionMappers, boolean orderExceptionMapperAfterClass) {
        this.exceptionMappers = exceptionMappers;
        this.orderExceptionMapperAfterClass = orderExceptionMapperAfterClass;
    }

    List<ExceptionMapperType> getExceptionMappers() {
        return Prioritizable.prioritze(exceptionMappers);
    }

	@SuppressWarnings({"rawtypes"})
    public Optional<ExceptionMapper> findMapperFor(Class<? extends Throwable> exceptionClass) {
		if( orderExceptionMapperAfterClass ) {
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
		} else {
			return getExceptionMappers().stream()
					.filter(mapperType -> mapperType.getExceptionClass().isAssignableFrom(exceptionClass))
					.map(ExceptionMapperType::getExceptionMapper)
					.findFirst();
		}
    }

    @SuppressWarnings({"rawtypes"})
    public <E extends Throwable> Optional<ExceptionMapper> findMapperFor(ErrorResponse errorResponse) {
		if( orderExceptionMapperAfterClass ) {
			int currentDepth = -1;
			ExceptionMapper closestExceptionMapper = null;

			for (ExceptionMapperType mapperType : exceptionMappers) {
				ExceptionMapper mapper = mapperType.getExceptionMapper();
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
			return (Optional) Optional.ofNullable(closestExceptionMapper);
		} else {
			return getExceptionMappers().stream()
					.map(ExceptionMapperType::getExceptionMapper)
					.filter(mapper -> mapper.accepts(errorResponse))
					.findFirst();
		}
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