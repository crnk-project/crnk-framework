package io.crnk.operations.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.operations.Operation;
import io.crnk.operations.OperationResponse;
import io.crnk.operations.internal.OperationParameterUtils;
import io.crnk.operations.server.order.OperationOrderStrategy;
import io.crnk.operations.server.order.OrderedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationsRequestProcessor implements HttpRequestProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationsRequestProcessor.class);

	private static final String JSONPATCH_CONTENT_TYPE = "application/json-patch+json";

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
				enrichTypeIdInformation(operations);

				OperationOrderStrategy orderStrategy = operationsModule.getOrderStrategy();
				List<OrderedOperation> orderedOperations = orderStrategy.order(operations);

				DefaultOperationFilterChain chain = new DefaultOperationFilterChain();
				List<OperationResponse> responses = chain.doFilter(new DefaultOperationFilterContext(orderedOperations));

				String responseJson = mapper.writeValueAsString(responses);
				context.setResponse(200, responseJson);
				context.setContentType(JSONPATCH_CONTENT_TYPE);
			}
			catch (Exception e) {
				LOGGER.error("failed to execute operations", e);
				context.setResponse(500, (byte[]) null);
			}
		}
	}


	private void enrichTypeIdInformation(List<Operation> operations) {
		ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
		for (Operation operation : operations) {
			if (operation.getOp().equalsIgnoreCase(HttpMethod.DELETE.toString())) {
				String path = OperationParameterUtils.parsePath(operation.getPath());
				JsonPath jsonPath = (new PathBuilder(resourceRegistry)).build(path);

				Resource resource = new Resource();
				resource.setType(jsonPath.getResourceName());
				resource.setId(jsonPath.getIds().getIds().get(0));
				operation.setValue(resource);
			}
		}
	}

	protected void fillinIgnoredOperations(OperationResponse[] responses) {
		for (int i = 0; i < responses.length; i++) {
			if (responses[i] == null) {
				OperationResponse operationResponse = new OperationResponse();
				operationResponse.setStatus(HttpStatus.PRECONDITION_FAILED_412);
				responses[i] = operationResponse;
			}
		}
	}

	protected List<OperationResponse> executeOperations(List<OrderedOperation> orderedOperations) {
		OperationResponse[] responses = new OperationResponse[orderedOperations.size()];
		boolean successful = true;
		for (OrderedOperation orderedOperation : orderedOperations) {
			OperationResponse operationResponse = executeOperation(orderedOperation.getOperation());
			responses[orderedOperation.getOrdinal()] = operationResponse;

			int status = operationResponse.getStatus();
			if (status >= 400) {
				successful = false;
				break;
			}
		}

		if (orderedOperations.size() > 1 && successful) {
			fetchUpToDateResponses(orderedOperations, responses);
		}

		fillinIgnoredOperations(responses);
		return Arrays.asList(responses);
	}

	protected void fetchUpToDateResponses(List<OrderedOperation> orderedOperations, OperationResponse[] responses) {
		RequestDispatcher requestDispatcher = moduleContext.getRequestDispatcher();

		// get current set of resources after all the updates have been applied
		for (OrderedOperation orderedOperation : orderedOperations) {
			Operation operation = orderedOperation.getOperation();
			OperationResponse operationResponse = responses[orderedOperation.getOrdinal()];

			boolean isPost = operation.getOp().equalsIgnoreCase(HttpMethod.POST.toString());
			boolean isPatch = operation.getOp().equalsIgnoreCase(HttpMethod.PATCH.toString());
			if (isPost || isPatch) {
				Resource resource = operationResponse.getSingleData().get();

				String path = resource.getType() + "/" + resource.getId();
				String method = HttpMethod.GET.toString();
				RepositoryMethodParameterProvider parameterProvider = null;

				Map<String, Set<String>> parameters = new HashMap<>();
				parameters.put("include", getLoadedRelationshipNames(resource));

				Response response =
						requestDispatcher.dispatchRequest(path, method, parameters, parameterProvider, null);
				copyDocument(operationResponse, response.getDocument());
				operationResponse.setIncluded(null);
			}
		}
	}

	protected OperationResponse executeOperation(Operation operation) {
		RequestDispatcher requestDispatcher = moduleContext.getRequestDispatcher();

		String path = OperationParameterUtils.parsePath(operation.getPath());
		Map<String, Set<String>> parameters = OperationParameterUtils.parseParameters(operation.getPath());
		String method = operation.getOp();
		RepositoryMethodParameterProvider parameterProvider = null;
		Document requestBody = new Document();
		requestBody.setData((Nullable) Nullable.of(operation.getValue()));

		Response response =
				requestDispatcher.dispatchRequest(path, method, parameters, parameterProvider, requestBody);
		OperationResponse operationResponse = new OperationResponse();
		operationResponse.setStatus(response.getHttpStatus());
		copyDocument(operationResponse, response.getDocument());
		return operationResponse;
	}

	private Set<String> getLoadedRelationshipNames(Resource resourceBody) {
		Set<String> result = new HashSet<>();
		for (Map.Entry<String, Relationship> entry : resourceBody.getRelationships().entrySet()) {
			if (entry.getValue() != null && entry.getValue().getData() != null) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	private void copyDocument(OperationResponse operationResponse, Document document) {
		if (document != null) {
			operationResponse.setData(document.getData());
			operationResponse.setMeta(document.getMeta());
			operationResponse.setLinks(document.getLinks());
			operationResponse.setErrors(document.getErrors());
			operationResponse.setIncluded(document.getIncluded());
		}
	}

	class DefaultOperationFilterContext implements OperationFilterContext {

		private List<OrderedOperation> orderedOperations;

		DefaultOperationFilterContext(List<OrderedOperation> orderedOperations) {
			this.orderedOperations = orderedOperations;
		}

		public List<OrderedOperation> getOrderedOperations() {
			return orderedOperations;
		}

		@Override
		public ServiceDiscovery getServiceDiscovery() {
			return moduleContext.getServiceDiscovery();
		}
	}

	class DefaultOperationFilterChain implements OperationFilterChain {

		protected int filterIndex = 0;

		@Override
		public List<OperationResponse> doFilter(OperationFilterContext context) {
			List<OperationFilter> filters = operationsModule.getFilters();
			if (filterIndex == filters.size()) {
				return executeOperations(context.getOrderedOperations());
			}
			else {
				OperationFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}
}
