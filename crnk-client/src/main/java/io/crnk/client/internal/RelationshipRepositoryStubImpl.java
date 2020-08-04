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
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class RelationshipRepositoryStubImpl<T, I, D, J> extends ClientStubBase
		implements RelationshipRepository<T, I, D, J> {

	private Class<T> sourceClass;

	private Class<D> targetClass;

	private ResourceInformation sourceResourceInformation;

	public RelationshipRepositoryStubImpl(CrnkClient client, Class<T> sourceClass, Class<D> targetClass,
										  ResourceInformation sourceResourceInformation, UrlBuilder urlBuilder,
										  boolean filterCriteriaInRequestBody) {
		super(client, urlBuilder, targetClass, filterCriteriaInRequestBody);
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
		this.sourceResourceInformation = sourceResourceInformation;
	}

	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(client.getQueryContext(), sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithId(url, HttpMethod.PATCH, targetId);
	}

	@Override
	public void setRelations(T source, Collection<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(client.getQueryContext(), sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.PATCH, targetIds);
	}

	@Override
	public void addRelations(T source, Collection<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(client.getQueryContext(), sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
		executeWithIds(url, HttpMethod.POST, targetIds);
	}

	@Override
	public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
		Serializable sourceId = getSourceId(source);
		String url = urlBuilder.buildUrl(client.getQueryContext(), sourceResourceInformation, sourceId, (QuerySpec) null, fieldName);
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
		verifyQuerySpec(querySpec);
		String url = urlBuilder.buildUrl(client.getQueryContext(), sourceResourceInformation, sourceId, querySpec, fieldName, false);
		String body = null;
		if (filterCriteriaInRequestBody) {
			body = serializeFilter(querySpec, sourceResourceInformation);
		}
		return (D) executeGet(url, body, ResponseType.RESOURCE);
	}

	@Override
	public DefaultResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		verifyQuerySpec(querySpec);
		String url = urlBuilder.buildUrl(client.getQueryContext(), sourceResourceInformation, sourceId, querySpec, fieldName, false);
		String body = null;
		if (filterCriteriaInRequestBody) {
			body = serializeFilter(querySpec, sourceResourceInformation);
		}
		return (DefaultResourceList<D>) executeGet(url, body, ResponseType.RESOURCES);
	}

	protected void verifyQuerySpec(QuerySpec querySpec) {
		Class<?> resourceClass = querySpec.getResourceClass();
		if (resourceClass != null && !resourceClass.equals(targetClass)) {
			throw new BadRequestException("resourceClass mismatch between repository and QuerySpec argument: "
					+ resourceClass + " vs " + targetClass);
		}
	}


	private void executeWithIds(String requestUrl, HttpMethod method, Collection<?> targetIds) {
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
