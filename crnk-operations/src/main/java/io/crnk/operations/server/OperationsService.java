package io.crnk.operations.server;

import io.crnk.operations.internal.PATCH;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

/**
 * Allows to execute multiple requests with a single using http://jsonpatch.com/.
 */
@Path("/operations")
public interface OperationsService {

	@PATCH
	@Path("/")
	@Consumes("application/json-patch+json")
	@Produces("application/json-patch+json")
	Response patch(String operations) throws IOException;

}
