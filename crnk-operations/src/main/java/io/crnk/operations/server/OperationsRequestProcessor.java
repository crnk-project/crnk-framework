package io.crnk.operations.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.module.Module;
import io.crnk.operations.Operation;
import io.crnk.operations.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            ObjectMapper mapper = moduleContext.getObjectMapper();
            try {
                List<Operation> operations = Arrays.asList(mapper.readValue(context.getRequestBody(), Operation[].class));

                QueryContext queryContext = context.getQueryContext();
                List<OperationResponse> responses = operationsModule.apply(operations, queryContext);

                String responseJson = mapper.writeValueAsString(responses);

                HttpResponse response = new HttpResponse();
                response.setStatusCode(200);
                response.setBody(responseJson);
                context.setResponse(response);
            } catch (Exception e) {
                Response response = toErrorResponse(e);
                HttpResponse httpResponse = response.toHttpResponse(mapper, HttpHeaders.JSONAPI_CONTENT_TYPE);
                context.setResponse(httpResponse);
            }
        }
    }

    private Response toErrorResponse(Throwable e) {
        ExceptionMapperRegistry exceptionMapperRegistry = moduleContext.getExceptionMapperRegistry();
        return exceptionMapperRegistry.toResponse(e);
    }
}
