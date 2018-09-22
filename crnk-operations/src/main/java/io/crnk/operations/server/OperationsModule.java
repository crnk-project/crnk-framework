package io.crnk.operations.server;

import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.utils.Nullable;
import io.crnk.operations.Operation;
import io.crnk.operations.OperationResponse;
import io.crnk.operations.internal.OperationParameterUtils;
import io.crnk.operations.server.order.DependencyOrderStrategy;
import io.crnk.operations.server.order.OperationOrderStrategy;
import io.crnk.operations.server.order.OrderedOperation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class OperationsModule implements Module {

	private OperationOrderStrategy orderStrategy = new DependencyOrderStrategy();

	private List<io.crnk.operations.server.OperationFilter> filters = new CopyOnWriteArrayList<>();

	private ModuleContext moduleContext;

	public static OperationsModule create() {
		return new OperationsModule();
	}

	// protected for CDI
	protected OperationsModule() {
	}

	public void addFilter(io.crnk.operations.server.OperationFilter filter) {
		this.filters.add(filter);
	}

	public void removeFilter(io.crnk.operations.server.OperationFilter filter) {
		this.filters.remove(filter);
	}

	public OperationOrderStrategy getOrderStrategy() {
		return orderStrategy;
	}

	public void setOrderStrategy(OperationOrderStrategy orderStrategy) {
		this.orderStrategy = orderStrategy;
	}

	public List<io.crnk.operations.server.OperationFilter> getFilters() {
		return filters;
	}

	@Override
	public String getModuleName() {
		return "operations";
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.moduleContext = context;
		context.addHttpRequestProcessor(new io.crnk.operations.server.OperationsRequestProcessor(this, context));
	}

	public List<OperationResponse> apply(List<Operation> operations) {
		enrichTypeIdInformation(operations);

		List<OrderedOperation> orderedOperations = orderStrategy.order(operations);

		DefaultOperationFilterChain chain = new DefaultOperationFilterChain();
		return chain.doFilter(new DefaultOperationFilterContext(orderedOperations));
	}

	private void enrichTypeIdInformation(List<Operation> operations) {
		ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
		for (Operation operation : operations) {
			if (operation.getOp().equalsIgnoreCase(HttpMethod.DELETE.toString())) {
				String path = OperationParameterUtils.parsePath(operation.getPath());
				JsonPath jsonPath = (new PathBuilder(resourceRegistry, moduleContext.getTypeParser())).build(path);

				RegistryEntry entry = jsonPath.getRootEntry();
				String idString = entry.getResourceInformation().toIdString(jsonPath.getId());

				Resource resource = new Resource();
				resource.setType(jsonPath.getRootEntry().getResourceInformation().getResourceType());
				resource.setId(idString);
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

				Map<String, Set<String>> parameters = new HashMap<>();
				parameters.put("include", getLoadedRelationshipNames(resource));

				Response response =
						requestDispatcher.dispatchRequest(path, method, parameters, null);
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
		Document requestBody = new Document();
		requestBody.setData(Nullable.of(operation.getValue()));

		Response response =
				requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
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

	protected class DefaultOperationFilterContext implements OperationFilterContext {

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

	protected class DefaultOperationFilterChain implements OperationFilterChain {

		protected int filterIndex = 0;

		@Override
		public List<OperationResponse> doFilter(OperationFilterContext context) {
			List<OperationFilter> filters = getFilters();
			if (filterIndex == filters.size()) {
				return executeOperations(context.getOrderedOperations());
			} else {
				OperationFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}
}
