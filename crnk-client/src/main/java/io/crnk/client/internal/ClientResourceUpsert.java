package io.crnk.client.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.client.ResponseBodyException;
import io.crnk.client.internal.proxy.ClientProxyFactory;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceUpsert;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClientResourceUpsert extends ResourceUpsert {

	private ClientProxyFactory proxyFactory;

	private Map<String, Object> resourceMap = new HashMap<>();

	public ClientResourceUpsert(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper, ClientProxyFactory proxyFactory) {
		super(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper);
		this.proxyFactory = proxyFactory;
	}

	public String getUID(ResourceIdentifier id) {
		return id.getType() + "#" + id.getId();
	}

	public String getUID(RegistryEntry entry, Serializable id) {
		return entry.getResourceInformation().getResourceType() + "#" + id;
	}

	public void setRelations(List<Resource> resources) {
		for (Resource resource : resources) {
			String uid = getUID(resource);
			Object object = resourceMap.get(uid);

			RegistryEntry registryEntry = resourceRegistry.getEntry(resource.getType());

			// no need for any query parameters when doing POST/PATCH
			QueryAdapter queryAdapter = null;

			// no in use on the client side
			RepositoryMethodParameterProvider parameterProvider = null;

			setRelations(object, registryEntry, resource, queryAdapter, parameterProvider);
		}
	}

	/**
	 * Get relations from includes section or create a remote proxy
	 */
	@Override
	protected Object fetchRelatedObject(RegistryEntry entry, Serializable relationId, RepositoryMethodParameterProvider parameterProvider, QueryAdapter queryAdapter) {

		String uid = getUID(entry, relationId);
		Object relatedResource = resourceMap.get(uid);
		if (relatedResource != null) {
			return relatedResource;
		}
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Class<?> resourceClass = resourceInformation.getResourceClass();
		return proxyFactory.createResourceProxy(resourceClass, relationId);
	}

	public List<Object> allocateResources(List<Resource> resources) {
		List<Object> objects = new ArrayList<>();
		for (Resource resource : resources) {

			RegistryEntry registryEntry = resourceRegistry.getEntry(resource.getType());
			if (registryEntry == null) {
				throw new RepositoryNotFoundException(resource.getType());
			}
			ResourceInformation resourceInformation = registryEntry.getResourceInformation();

			Object object = newResource(resourceInformation, resource);
			setId(resource, object, resourceInformation);
			setAttributes(resource, object, resourceInformation);
			setLinks(resource, object, resourceInformation);
			setMeta(resource, object, resourceInformation);

			objects.add(object);

			String uid = getUID(resource);
			resourceMap.put(uid, object);
		}
		return objects;
	}

	protected void setLinks(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		ResourceField linksField = resourceInformation.getLinksField();
		if (dataBody.getLinks() != null && linksField != null) {
			JsonNode linksNode = dataBody.getLinks();
			Class<?> linksClass = linksField.getType();
			ObjectReader linksMapper = objectMapper.readerFor(linksClass);
			try {
				Object links = linksMapper.readValue(linksNode);
				linksField.getAccessor().setValue(instance, links);
			} catch (IOException e) {
				throw new ResponseBodyException("failed to parse links information", e);
			}
		}
	}

	protected void setMeta(Resource dataBody, Object instance, ResourceInformation resourceInformation) {
		ResourceField metaField = resourceInformation.getMetaField();
		if (dataBody.getMeta() != null && metaField != null) {
			JsonNode metaNode = dataBody.getMeta();

			Class<?> metaClass = metaField.getType();

			ObjectReader metaMapper = objectMapper.readerFor(metaClass);
			try {
				Object meta = metaMapper.readValue(metaNode);
				metaField.getAccessor().setValue(instance, meta);
			} catch (IOException e) {
				throw new ResponseBodyException("failed to parse links information", e);
			}

		}
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		// no in use on client side, consider refactoring ResourceUpsert to
		// separate from controllers
		throw new UnsupportedOperationException();
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document document) {
		// no in use on client side, consider refactoring ResourceUpsert to
		// separate from controllers
		throw new UnsupportedOperationException();
	}

	@Override
	protected void setRelationsField(Object newResource, RegistryEntry registryEntry, Map.Entry<String, Relationship> property, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {

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
					String url = relatedNode.asText().trim();
					Object proxy = proxyFactory.createCollectionProxy(elementType, collectionClass, url);
					field.getAccessor().setValue(newResource, proxy);
				}
			}
		} else {
			// set elements
			super.setRelationsField(newResource, registryEntry, property, queryAdapter, parameterProvider);
		}
	}

	@Override
	protected boolean canModifyField(ResourceInformation resourceInformation, String fieldName, ResourceField field) {
		// nothing to verify during deserialization on client-side
		// there is only a need to check field access when receiving resources
		// on the server-side client needs all the data he gets from the server
		return true;
	}
}
