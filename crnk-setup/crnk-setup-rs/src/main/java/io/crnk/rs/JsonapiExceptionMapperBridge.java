package io.crnk.rs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.rs.type.JsonApiMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to return JAXRS exceptions in the JSON API format.
 */
public class JsonapiExceptionMapperBridge implements ExceptionMapper<RuntimeException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonapiExceptionMapperBridge.class);

	private CrnkFeature feature;

	public JsonapiExceptionMapperBridge(CrnkFeature feature) {
		this.feature = feature;
	}

	@Override
	public Response toResponse(RuntimeException exception) {
		CrnkBoot boot = this.feature.getBoot();
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		Optional<io.crnk.core.engine.error.ExceptionMapper> optional = exceptionMapperRegistry.findMapperFor(exception.getClass());

		if (!optional.isPresent()) {
			LOGGER.error("no exception mapper found", exception);
			exception = new InternalServerErrorException(exception.getMessage());
			optional = exceptionMapperRegistry.findMapperFor(exception.getClass());
		}
		io.crnk.core.engine.error.ExceptionMapper exceptionMapper = optional.get();
		ErrorResponse errorResponse = exceptionMapper.toErrorResponse(exception);

		// use the Crnk document mapper to create a JSON API response
		Document doc = new Document();

		List<ErrorData> errors = new ArrayList<>();
		for (ErrorData error : errorResponse.getErrors()) {
			errors.add(error);
		}
		doc.setErrors(errors);

		return Response.status(errorResponse.getHttpStatus()).entity(doc).header("Content-Type", JsonApiMediaType.APPLICATION_JSON_API).build();
	}

}
