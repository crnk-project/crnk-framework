package io.crnk.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.CrnkClient;
import io.crnk.client.RelationshipRepositoryStub;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;
import java.util.ArrayList;

public class RelationshipRepositoryStubImpl<T, I extends Serializable, D, J extends Serializable> extends AbstractStub implements RelationshipRepositoryStub<T, I, D, J>, RelationshipRepositoryV2<T, I, D, J> {

	private Class<T> sourceClass;

	private Class<D> targetClass;

	private ResourceInformation sourceResourceInformation;

	private RegistryEntry relationshipEntry;

	public RelationshipRepositoryStubImpl(CrnkClient client, Class<T> sourceClass, Class<D> targetClass, ResourceInformation sourceResourceInformation, JsonApiUrlBuilder urlBuilder, RegistryEntry relationshipEntry) {
		super(client, urlBuilder);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		this.sourceResourceInformation = sourceResourceInformation;
		this.relationshipEntry = relationshipEntry;
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
		ResourceField idField = sourceResourceInformation.getIdField();
		return (Serializable) idField.getAccessor().getValue(source);
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, queryParams, fieldName);
		return (D) executeGet(url, ResponseType.RESOURCE);
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QueryParams queryParams) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, queryParams, fieldName);
		return (DefaultResourceList<D>) executeGet(url, ResponseType.RESOURCES);
	}

	@SuppressWarnings("unchecked")
	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, querySpec, fieldName);
		return (D) executeGet(url, ResponseType.RESOURCE);
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		String url = urlBuilder.buildUrl(sourceResourceInformation, sourceId, querySpec, fieldName);
		return (DefaultResourceList<D>) executeGet(url, ResponseType.RESOURCES);
	}

	private void executeWithIds(String requestUrl, HttpMethod method, Iterable<?> targetIds) {
		Document document = new Document();
		ArrayList<ResourceIdentifier> resourceIdentifiers = new ArrayList<>();
		for (Object targetId : targetIds) {
			String strTargetId = sourceResourceInformation.toIdString(targetId);
			resourceIdentifiers.add(new ResourceIdentifier(strTargetId, sourceResourceInformation.getResourceType()));
		}
		document.setData(Nullable.of((Object) resourceIdentifiers));
		doExecute(requestUrl, method, document);
	}

	private void executeWithId(String requestUrl, HttpMethod method, Object targetIds) {
		Document document = new Document();
		String strTargetId = sourceResourceInformation.toIdString(targetIds);
		ResourceIdentifier resourceIdentifier = new ResourceIdentifier(strTargetId, sourceResourceInformation.getResourceType());
		document.setData(Nullable.of((Object) resourceIdentifier));
		doExecute(requestUrl, method, document);
	}

	private void doExecute(String requestUrl, HttpMethod method, Document document) {
		ObjectMapper objectMapper = client.getObjectMapper();
		String requestBodyValue;
		try {
			requestBodyValue = objectMapper.writeValueAsString(document);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
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
