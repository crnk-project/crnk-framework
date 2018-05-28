package io.crnk.client.internal;

import java.util.ArrayList;
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
import io.crnk.core.engine.internal.dispatcher.controller.ControllerContext;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMapperUtil;
import io.crnk.core.engine.internal.document.mapper.ResourceMapper;
import io.crnk.core.engine.internal.document.mapper.ResourceMappingConfig;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Nullable;

public class ClientDocumentMapper extends DocumentMapper {

	private final ModuleRegistry moduleRegistry;

	private ClientProxyFactory proxyFactory;

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private TypeParser typeParser;

	public ClientDocumentMapper(ModuleRegistry moduleRegistry, ObjectMapper objectMapper, PropertiesProvider
			propertiesProvider) {
		super(moduleRegistry.getResourceRegistry(), objectMapper, propertiesProvider, null, new ImmediateResultFactory(), null, true);
		this.moduleRegistry = moduleRegistry;
		this.resourceRegistry = moduleRegistry.getResourceRegistry();
		this.typeParser = moduleRegistry.getTypeParser();
		this.objectMapper = objectMapper;
	}

	@Override
	protected ResourceMapper newResourceMapper(final DocumentMapperUtil util, boolean client, ObjectMapper objectMapper) {
		return new ResourceMapper(util, client, objectMapper, null) {

			@Override
			protected void setRelationship(Resource resource, ResourceField field, Object entity,
										   ResourceInformation resourceInformation, QueryAdapter queryAdapter,
										   ResourceMappingConfig mappingConfig) {
				// we also include relationship data if it is not null and not a
				// unloaded proxy
				boolean includeRelation = true;

				Object relationshipId = null;

				if (field.hasIdField()) {
					Object relationshipValue = field.getIdAccessor().getValue(entity);

					ResourceInformation oppositeInformation =
							resourceRegistry.getEntry(field.getOppositeResourceType()).getResourceInformation();

					if (relationshipValue instanceof Collection) {
						List ids = new ArrayList();
						for (Object elem : (Collection<?>) relationshipValue) {
							ids.add(oppositeInformation.toResourceIdentifier(elem));
						}
						relationshipId = ids;
					} else if (relationshipValue != null) {
						relationshipId = oppositeInformation.toResourceIdentifier(relationshipValue);
					}

					includeRelation = relationshipId != null || field.getSerializeType() != SerializeType.LAZY;
				} else {
					Object relationshipValue = field.getAccessor().getValue(entity);
					if (relationshipValue instanceof ObjectProxy) {
						includeRelation = ((ObjectProxy) relationshipValue).isLoaded();
					} else {
						// TODO for fieldSets handling in the future the lazy
						// handling must be different
						includeRelation = relationshipValue != null || field.getSerializeType() != SerializeType.LAZY && !field
								.isCollection();
					}

					if (relationshipValue != null && includeRelation) {
						if (relationshipValue instanceof Collection) {
							relationshipId = util.toResourceIds((Collection<?>) relationshipValue);
						} else {
							relationshipId = util.toResourceId(relationshipValue);
						}
					}
				}


				if (includeRelation) {
					Relationship relationship = new Relationship();
					relationship.setData(Nullable.of((Object) relationshipId));
					resource.getRelationships().put(field.getJsonName(), relationship);
				}
			}
		};
	}

	public void setProxyFactory(ClientProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public Object fromDocument(Document document, boolean getList) {
		ControllerContext controllerContext = new ControllerContext(moduleRegistry, () -> this);
		ClientResourceUpsert upsert = new ClientResourceUpsert(proxyFactory);
		upsert.init(controllerContext);

		PreconditionUtil.verify(document.getErrors() == null || document.getErrors().isEmpty(), "document contains json api errors and cannot be processed, use exception mapper instead");

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
		} else {
			if (dataObjects.isEmpty()) {
				return null;
			}
			PreconditionUtil.verify(dataObjects.size() == 1, "expected unique result, got %s", dataObjects);
			return dataObjects.get(0);
		}
	}

}
