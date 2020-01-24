package io.crnk.core.engine.internal.http;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.path.ActionPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonApiRequestProcessor extends JsonApiRequestProcessorBase implements HttpRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonApiRequestProcessor.class);

    public JsonApiRequestProcessor(Module.ModuleContext moduleContext) {
        super(moduleContext);
    }

    public static boolean matchesContentTypeHeader(HttpRequestContext requestContext, boolean acceptPlainJson) {
        String method = requestContext.getMethod().toUpperCase();
        boolean isPatch = method.equals(HttpMethod.PATCH.toString());
        boolean isPost = method.equals(HttpMethod.POST.toString());
        if (isPatch || isPost) {
            String contentType = requestContext.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE);
            if (contentType == null || !contentType.startsWith(HttpHeaders.JSONAPI_CONTENT_TYPE)) {
                LOGGER.debug("not a JSON-API request due to content type {}", contentType);
                return false;
            }
        }
        return true;
    }

    public static boolean matchesAcceptHeader(HttpRequestContext requestContext, boolean acceptPlainJson) {
        // short-circuit each of the possible Accept MIME type checks, so that we don't keep comparing after we have already
        // found a match. Intentionally kept as separate statements (instead of a big, chained ||) to ease debugging/maintenance.
        boolean acceptsJsonApi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
        boolean acceptsAny = acceptsJsonApi || requestContext.acceptsAny();

        boolean acceptsPlainJson = acceptsAny || (acceptPlainJson && requestContext.accepts("application/json"));
        LOGGER.debug("HTTP Accept header matches: {}", acceptPlainJson);
        return acceptsPlainJson;
    }


    /**
     * Determines whether the supplied HTTP request is considered a JSON-API request.
     *
     * @param requestContext  The HTTP request
     * @param acceptPlainJson Whether a plain JSON request should also be considered a JSON-API request
     * @return <code>true</code> if it is a JSON-API request; <code>false</code> otherwise
     * @since 2.4
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static boolean isJsonApiRequest(HttpRequestContext requestContext, boolean acceptPlainJson) {
        return matchesContentTypeHeader(requestContext, acceptPlainJson) && matchesAcceptHeader(requestContext, acceptPlainJson);
    }


    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public boolean accepts(HttpRequestContext context) {
        boolean acceptingPlainJson = isAcceptingPlainJson();
        boolean acceptHeaderMatch = matchesAcceptHeader(context, acceptingPlainJson);
        if (acceptHeaderMatch) {
            boolean contentTypeHeaderMatch = matchesContentTypeHeader(context, acceptingPlainJson);
            JsonPath jsonPath = helper.getJsonPath(context);
            if (jsonPath == null) {
                LOGGER.debug("not accepted since no matching repository defined for path={}", context.getPath());
                return false;
            }
            if (!contentTypeHeaderMatch) {
                LOGGER.warn("not accepted due to content-type header mismatch, " + HttpHeaders.JSONAPI_CONTENT_TYPE + " missing?");
                return false;
            }
            LOGGER.debug("accepted to server request: path={}", jsonPath);
            return true;
        }
        return false;
    }

    @Override
    public Result<HttpResponse> processAsync(HttpRequestContext requestContext) {
        Result<HttpResponse> response = checkMethod(requestContext);
        if (response != null) {
            return response;
        }

        String method = requestContext.getMethod();
        ResultFactory resultFactory = moduleContext.getResultFactory();

        JsonPath jsonPath = helper.getJsonPath(requestContext);
        LOGGER.debug("processing JSON API request path={}, method={}", jsonPath, method);
        Map<String, Set<String>> parameters = requestContext.getRequestParameters();

        if (jsonPath instanceof ActionPath) {
            // inital implementation, has to improve
            String path = requestContext.getPath();
            RequestDispatcher requestDispatcher = moduleContext.getRequestDispatcher();
            requestDispatcher.dispatchAction(path, method, parameters);
            return null;
        } else if (jsonPath != null) {
            Document requestDocument;
            try {
                requestDocument = getRequestDocument(requestContext);
            } catch (JsonProcessingException e) {
                return resultFactory.just(getErrorResponse(e));
            }
            QueryContext queryContext = requestContext.getQueryContext();
            return processAsync(jsonPath, method, parameters, requestDocument, queryContext)
                    .map(this::toHttpResponse);
        }
        return resultFactory.just(buildMethodNotAllowedResponse(method));
    }

    private Result<HttpResponse> checkMethod(HttpRequestContext requestContext) {
        String method = requestContext.getMethod();
        boolean isPatch = method.equals(HttpMethod.PATCH.toString());
        boolean isPost = method.equals(HttpMethod.POST.toString());
        boolean isGet = method.equals(HttpMethod.GET.toString());
        boolean isDelete = method.equals(HttpMethod.DELETE.toString());
        boolean acceptsMethod = isGet || isDelete || isPatch || isPost;
        if (!acceptsMethod) {
            ResultFactory resultFactory = moduleContext.getResultFactory();
            return resultFactory.just(buildMethodNotAllowedResponse(method));
        }
        return null;
    }


    public Result<Response> processAsync(JsonPath jsonPath, String method, Map<String, Set<String>> parameters,
                                         Document requestDocument, QueryContext queryContext) {
        try {
            ResultFactory resultFactory = moduleContext.getResultFactory();
            QueryAdapter queryAdapter = helper.toQueryAdapter(parameters, jsonPath, queryContext);

            LOGGER.debug("using requestVersion={}", queryContext.getRequestVersion());

            if (resultFactory instanceof ImmediateResultFactory) {
                LOGGER.debug("processing synchronously");
                // not that document filters are not compatible with async programming
                DocumentFilterContextImpl filterContext =
                        new DocumentFilterContextImpl(jsonPath, queryAdapter, requestDocument, method);
                try {
                    DocumentFilterChain filterChain = getFilterChain(jsonPath, method);
                    Response response = filterChain.doFilter(filterContext);
                    return resultFactory.just(response);
                } catch (Exception e) {
                    Response response = toErrorResponse(e);
                    return resultFactory.just(response);
                }
            } else {
                LOGGER.debug("processing asynchronously");
                ControllerRegistry controllerRegistry = moduleContext.getModuleRegistry().getControllerRegistry();
                Controller controller = controllerRegistry.getController(jsonPath, method);
                Result<Response> responseResult =
                        controller.handleAsync(jsonPath, queryAdapter, requestDocument);
                return responseResult.onErrorResume(this::toErrorResponse);
            }
        } catch (Exception e) {
            ResultFactory resultFactory = moduleContext.getResultFactory();
            return resultFactory.just(toErrorResponse(e));
        }
    }

    private Response toErrorResponse(Throwable e) {
        return moduleContext.getExceptionMapperRegistry().toErrorResponse(e);
    }

    protected DocumentFilterChain getFilterChain(JsonPath jsonPath, String method) {
        ControllerRegistry controllerRegistry = moduleContext.getModuleRegistry().getControllerRegistry();
        Controller controller = controllerRegistry.getController(jsonPath, method);
        return new DocumentFilterChainImpl(moduleContext, controller);

    }


}
