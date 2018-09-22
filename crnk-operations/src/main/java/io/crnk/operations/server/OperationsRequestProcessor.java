package io.crnk.operations.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.module.Module;
import io.crnk.operations.Operation;
import io.crnk.operations.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OperationsRequestProcessor implements HttpRequestProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationsRequestProcessor.class);

	public static final String JSONPATCH_CONTENT_TYPE = "application/json-patch+json";

	private Module.ModuleContext moduleContext;

	private OperationsModule operationsModule;

	public OperationsRequestProcessor(OperationsModule operationsModule, Module.ModuleContext moduleContext) {
		this.operationsModule = operationsModule;
		this.moduleContext = moduleContext;
	}

	@Override
	public void process(HttpRequestContext context) throws IOException {
		if (context.accepts(JSONPATCH_CONTENT_TYPE)) {
			try {
				ObjectMapper mapper = moduleContext.getObjectMapper();

				List<Operation> operations = Arrays.asList(mapper.readValue(context.getRequestBody(), Operation[].class));

				List<OperationResponse> responses = operationsModule.apply(operations);

				String responseJson = mapper.writeValueAsString(responses);
				context.setContentType(JSONPATCH_CONTENT_TYPE);
				context.setResponse(200, responseJson);
			} catch (Exception e) {
				LOGGER.error("failed to execute operations", e);
				context.setResponse(500, (byte[]) null);
			}
		}
	}

}
