package io.crnk.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.CrnkClient;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class RelationshipRepositoryStubImpl<T, I extends Serializable, D, J extends Serializable> extends ClientStubBase
		implements RelationshipRepository<T, I, D, J> {

	private Class<T> sourceClass;

	private Class<D> targetClass;

	private ResourceInformation sourceResourceInformation;

	public RelationshipRepositoryStubImpl(CrnkClient client, Class<T> sourceClass, Class<D> targetClass,
										  ResourceInformation sourceResourceInformation, JsonApiUrlBuilder urlBuilder) {
		super(client, urlBuilder, targetClass);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		this.sourceResourceInformation = sourceResourceInformation;
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithId(url, HttpMethod.PATCH, targetId);
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.PATCH, targetIds);
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.POST, targetIds);
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.DELETE, targetIds);
	}

	private Serializable getSourceId(T source) {
		if (source instanceof Resource) {
			return ((Resource) source).getId();
		}
		ResourceField idField = sourceResourceInformation.getIdField();
		return (Serializable) idField.getAccessor().getValue(source);
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, querySpec, fieldName, false);
		return (D) executeGet(url, ResponseType.RESOURCE);
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, querySpec, fieldName, false);
		return (DefaultResourceList<D>) executeGet(url, ResponseType.RESOURCES);
	}

	private void executeWithIds(String requestUrl, HttpMethod method, Iterable<?> targetIds) {
		Document document = new Document();
		ArrayList<ResourceIdentifier> resourceIdentifiers = new ArrayList<>();
		for (Object targetId : targetIds) {
			resourceIdentifiers.add(sourceResourceInformation.toResourceIdentifier(targetId));
		}
		document.setData(Nullable.of(resourceIdentifiers));
		Document transportDocument = client.getFormat().toTransportDocument(document);
		doExecute(requestUrl, method, transportDocument);
	}

	private void executeWithId(String requestUrl, HttpMethod method, Object targetId) {
		Document document = new Document();
		ResourceIdentifier resourceIdentifier = sourceResourceInformation.toResourceIdentifier(targetId);
		document.setData(Nullable.of(resourceIdentifier));
		Document transportDocument = client.getFormat().toTransportDocument(document);
		doExecute(requestUrl, method, transportDocument);
	}

	private void doExecute(String requestUrl, HttpMethod method, final Document document) {
		final ObjectMapper objectMapper = client.getObjectMapper();
		String requestBodyValue = ExceptionUtil.wrapCatchedExceptions(() -> objectMapper.writeValueAsString(document));
		execute(requestUrl, ResponseType.NONE, method, requestBodyValue);
	}

	@Override
	public Class<T> getSourceResourceClass() {
		return sourceClass;
	}

	@Override
	public Class<D> getTargetResourceClass() {
		return targetClass;
	}
}
