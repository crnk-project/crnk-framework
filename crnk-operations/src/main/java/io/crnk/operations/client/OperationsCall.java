package io.crnk.operations.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.client.internal.ClientDocumentMapper;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.operations.Operation;
import io.crnk.operations.OperationResponse;
import io.crnk.operations.server.OperationsRequestProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperationsCall {

	private OperationsClient client;

	private List<QueuedOperation> queuedOperations = new ArrayList<>();

	private List<OperationResponse> responses;

	protected OperationsCall(OperationsClient client) {
		this.client = client;
	}

	public void add(HttpMethod method, Object object) {
		Operation operation = new Operation();

		Resource resource = toResource(object);

		operation.setOp(method.toString());
		operation.setPath(computePath(method, resource));
		if (method == HttpMethod.POST || method == HttpMethod.PATCH) {
			operation.setValue(resource);
		}

		QueuedOperation queuedOperation = new QueuedOperation();
		queuedOperation.operation = operation;
		queuedOperations.add(queuedOperation);
	}

	protected String computePath(HttpMethod method, Resource resource) {
		if (method == HttpMethod.POST) {
			return resource.getType();
		}
		return resource.getType() + "/" + resource.getId() + "/";
	}

	protected Resource toResource(Object object) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(object);

		QuerySpec querySpec = new QuerySpec(object.getClass());
		CrnkClient crnk = client.getCrnk();
		QueryContext queryContext = crnk.getQueryContext();
		ResourceRegistry registry = crnk.getRegistry();

		QueryAdapter queryAdapter = new QuerySpecAdapter(querySpec, registry, queryContext);

		DocumentMapper documentMapper = crnk.getDocumentMapper();
		DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
		Document document = documentMapper.toDocument(response, queryAdapter, mappingConfig).get();
		return document.getSingleData().get();
	}

	protected <T> T fromResource(Document document, Class<T> clazz) {
		CrnkClient crnk = client.getCrnk();
		ClientDocumentMapper documentMapper = crnk.getDocumentMapper();
		return (T) documentMapper.fromDocument(document, false);
	}

	public void execute() {
		List<Operation> operations = new ArrayList<>();
		for (QueuedOperation queuedOperation : queuedOperations) {
			operations.add(queuedOperation.operation);
		}

		HttpAdapter adapter = client.getCrnk().getHttpAdapter();
		ObjectMapper mapper = client.getCrnk().getObjectMapper();
		try {
			String operationsJson = mapper.writer().writeValueAsString(operations.toArray(new Operation[operations.size()]));

			String url = client.getCrnk().getServiceUrlProvider().getUrl() + "/operations";
			HttpAdapterRequest request = adapter.newRequest(url, HttpMethod.PATCH, operationsJson);
			request.header(HttpHeaders.HTTP_CONTENT_TYPE, OperationsRequestProcessor.JSONPATCH_CONTENT_TYPE);
			request.header(HttpHeaders.HTTP_HEADER_ACCEPT, OperationsRequestProcessor.JSONPATCH_CONTENT_TYPE);
			HttpAdapterResponse response = request.execute();

			int status = response.code();
			if (status != 200) {
				// general issue, status of individual operations is important.
				throw new InternalServerErrorException("patch execution failed with status " + status);
			}
			String json = response.body();
			responses = Arrays.asList(mapper.readValue(json, OperationResponse[].class));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public OperationResponse getResponse(int index) {
		checkResponsesAvailable();
		return responses.get(index);
	}

	public <T> T getResponseObject(int index, Class<T> clazz) {
		checkResponsesAvailable();
		OperationResponse response = responses.get(index);
		return fromResource(response, clazz);
	}

	private void checkResponsesAvailable() {
		if (responses == null) {
			throw new IllegalStateException("response not yet available, wait for execute() to finish");
		}
	}

	private class QueuedOperation {

		private Operation operation;

	}
}
