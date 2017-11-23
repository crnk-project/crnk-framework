package io.crnk.client.internal;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.internal.proxy.ClientProxyFactory;
import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.client.response.JsonLinksInformation;
import io.crnk.client.response.JsonMetaInformation;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperUtil;
import io.crnk.core.engine.internal.document.mapper.ResourceMapper;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Nullable;

public class ClientDocumentMapper extends DocumentMapper {

	private ClientProxyFactory proxyFactory;

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private TypeParser typeParser;

	public ClientDocumentMapper(ModuleRegistry moduleRegistry, ObjectMapper objectMapper, PropertiesProvider
			propertiesProvider) {
		super(moduleRegistry.getResourceRegistry(), objectMapper, propertiesProvider, null,true);
		this.resourceRegistry = moduleRegistry.getResourceRegistry();
		this.typeParser = moduleRegistry.getTypeParser();
		this.objectMapper = objectMapper;
	}

	@Override
	protected ResourceMapper newResourceMapper(final DocumentMapperUtil util, boolean client, ObjectMapper objectMapper) {
		return new ResourceMapper(util, client, objectMapper, null) {

			@Override
			protected void setRelationship(Resource resource, ResourceField field, Object entity,
					ResourceInformation resourceInformation, QueryAdapter queryAdapter) {
				// we also include relationship data if it is not null and not a
				// unloaded proxy
				boolean includeRelation = true;
				Object relationshipValue = field.getAccessor().getValue(entity);
				if (relationshipValue instanceof ObjectProxy) {
					includeRelation = ((ObjectProxy) relationshipValue).isLoaded();
				}
				else {
					// TODO for fieldSets handling in the future the lazy
					// handling must be different
					includeRelation = relationshipValue != null || field.getSerializeType() != SerializeType.LAZY && !field.isCollection();
				}

				if (includeRelation) {
					Relationship relationship = new Relationship();
					if (relationshipValue instanceof Collection) {
						relationship.setData(Nullable.of((Object) util.toResourceIds((Collection<?>) relationshipValue)));
					}
					else {
						relationship.setData(Nullable.of((Object) util.toResourceId(relationshipValue)));
					}
					resource.getRelationships().put(field.getJsonName(), relationship);
				}
			}
		};
	}

	public void setProxyFactory(ClientProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public Object fromDocument(Document document, boolean getList) {
		ClientResourceUpsert upsert =
				new ClientResourceUpsert(resourceRegistry, propertiesProvider, typeParser, objectMapper, null, proxyFactory);

		PreconditionUtil.assertFalse("document contains json api errors and cannot be processed",
				document.getErrors() != null && !document.getErrors().isEmpty());

		if (!document.getData().isPresent()) {
			return null;
		}

		List<Resource> included = document.getIncluded();
		List<Resource> data = document.getCollectionData().get();

		List<Object> dataObjects = upsert.allocateResources(data);
		if (included != null) {
			upsert.allocateResources(included);
		}

		upsert.setRelations(data);
		if (included != null) {
			upsert.setRelations(included);
		}

		if (getList) {
			DefaultResourceList<Object> resourceList = new DefaultResourceList();
			resourceList.addAll(dataObjects);
			if (document.getLinks() != null) {
				resourceList.setLinks(new JsonLinksInformation(document.getLinks(), objectMapper));
			}
			if (document.getMeta() != null) {
				resourceList.setMeta(new JsonMetaInformation(document.getMeta(), objectMapper));
			}
			return resourceList;
		}
		else {
			if (dataObjects.isEmpty()) {
				return null;
			}
			PreconditionUtil.assertFalse("expected unique result", dataObjects.size() > 1);
			return dataObjects.get(0);
		}
	}

}
