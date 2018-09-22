package io.crnk.client.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.client.ResponseBodyException;
import io.crnk.client.internal.proxy.ClientProxyFactory;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceUpsert;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;

class ClientResourceUpsert extends ResourceUpsert {

	private ClientProxyFactory proxyFactory;

	private Map<String, Object> resourceMap = new HashMap<>();

	public ClientResourceUpsert(ClientProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public String getUID(ResourceIdentifier id) {
		return id.getType() + "#" + id.getId();
	}

	public String getUID(RegistryEntry entry, Serializable id) {
		ResourceInformation resourceInformation = entry.getResourceInformation();
		String idString = resourceInformation.toIdString(id);
		return resourceInformation.getResourceType() + "#" + idString;
	}

	public void setRelations(List<Resource> resources) {
		for (Resource resource : resources) {
			String uid = getUID(resource);
			Object object = resourceMap.get(uid);

			RegistryEntry registryEntry = context.getResourceRegistry().getEntry(resource.getType());

			// no need for any query parameters when doing POST/PATCH
			QueryAdapter queryAdapter = null;

			setRelationsAsync(object, registryEntry, resource, queryAdapter, true).get();
		}
	}

	/**
	 * Get relations from includes section or create a remote proxy
	 */
	@Override
	protected Result<Object> fetchRelated(RegistryEntry entry, Serializable relationId, QueryAdapter queryAdapter) {

		ResultFactory resultFactory = context.getResultFactory();

		String uid = getUID(entry, relationId);
		Object relatedResource = resourceMap.get(uid);
		if (relatedResource != null) {
			return resultFactory.just(relatedResource);
		}
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Class<?> resourceClass = resourceInformation.getResourceClass();
		return resultFactory.just(proxyFactory.createResourceProxy(resourceClass, relationId));
	}

	@Override
	protected boolean decideSetRelationObjectField(RegistryEntry entry, Serializable relationId, ResourceField field) {
		return !field.hasIdField() || resourceMap.containsKey(getUID(entry, relationId));
	}

	@Override
	protected boolean decideSetRelationObjectsField(ResourceField relationshipField) {
		return true;
	}

	public List<Object> allocateResources(List<Resource> resources) {
		List<Object> objects = new ArrayList<>();
		for (Resource resource : resources) {

			RegistryEntry registryEntry = getRegistryEntry(resource.getType());
			ResourceInformation resourceInformation = registryEntry.getResourceInformation();

			Object object = newEntity(resourceInformation, resource);
			setId(resource, object, resourceInformation);
			setAttributes(resource, object, resourceInformation, new QueryContext());
			setLinks(resource, object, resourceInformation);
			setMeta(resource, object, resourceInformation);

			objects.add(object);

			String uid = getUID(resource);
			resourceMap.put(uid, object);
		}
		return objects;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		// no in use on client side, consider refactoring ResourceUpsert to
		// separate from controllers
		throw new UnsupportedOperationException();
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {
		// no in use on client side, consider refactoring ResourceUpsert to
		// separate from controllers
		throw new UnsupportedOperationException();

	}

	@Override
	protected RuntimeException newBodyException(String message, IOException e) {
		throw new ResponseBodyException(message, e);
	}

	@Override
	protected Optional<Result> setRelationsFieldAsync(Object newResource, RegistryEntry registryEntry,
			Map.Entry<String, Relationship> property, QueryAdapter queryAdapter) {

		Relationship relationship = property.getValue();

		if (!relationship.getData().isPresent()) {
			ObjectNode links = relationship.getLinks();
			if (links != null) {
				// create proxy to lazy load relations
				String fieldName = property.getKey();
				ResourceInformation resourceInformation = registryEntry.getResourceInformation();
				ResourceField field = resourceInformation.findRelationshipFieldByName(fieldName);
				Class elementType = field.getElementType();
				Class collectionClass = field.getType();

				JsonNode relatedNode = links.get("related");
				if (relatedNode != null) {
					String url = null;
					if (relatedNode.has(SerializerUtil.HREF)) {
						JsonNode hrefNode = relatedNode.get(SerializerUtil.HREF);
						if (hrefNode != null) {
							url = hrefNode.asText().trim();
						}
					}
					else {
						url = relatedNode.asText().trim();
					}
					Object proxy = proxyFactory.createCollectionProxy(elementType, collectionClass, url);
					field.getAccessor().setValue(newResource, proxy);
				}
			}
			return Optional.empty();
		}
		else {
			// set elements
			return super.setRelationsFieldAsync(newResource, registryEntry, property, queryAdapter);
		}
	}

	@Override
	protected boolean canModifyField(ResourceInformation resourceInformation, String fieldName, ResourceField field,
			QueryContext queryContext) {
		// nothing to validate during deserialization on client-side
		// there is only a need to check field access when receiving resources
		// on the server-side client needs all the data he gets from the server
		return true;
	}

	@Override
	protected HttpMethod getHttpMethod() {
		throw new UnsupportedOperationException();
	}
}
