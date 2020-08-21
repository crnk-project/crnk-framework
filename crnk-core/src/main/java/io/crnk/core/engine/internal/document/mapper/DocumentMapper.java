package io.crnk.core.engine.internal.document.mapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.utils.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentMapper.class);

	protected final ResourceFilterDirectory resourceFilterDirectory;

	private ObjectNode jsonapi;

	protected PropertiesProvider propertiesProvider;

	private DocumentMapperUtil util;

	private ResourceMapper resourceMapper;

	private IncludeLookupSetter includeLookupSetter;

	private ResultFactory resultFactory;

	private boolean client;

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider,
			ResourceFilterDirectory resourceFilterDirectory, ResultFactory resultFactory, Map<String, String> serverInfo, UrlBuilder urlBuilder) {
		this(resourceRegistry, objectMapper, propertiesProvider, resourceFilterDirectory, resultFactory, serverInfo, false, urlBuilder);
	}

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider,
			ResourceFilterDirectory resourceFilterDirectory, ResultFactory resultFactory, Map<String, String> serverInfo,
			boolean client, UrlBuilder urlBuilder) {
		this.propertiesProvider = propertiesProvider;
		this.client = client;
		this.resultFactory = resultFactory;
		this.resourceFilterDirectory = resourceFilterDirectory;

		PreconditionUtil.verify(client || resourceFilterDirectory != null, "filterBehavior necessary on server-side");

		this.util = newDocumentMapperUtil(resourceRegistry, objectMapper, propertiesProvider, urlBuilder);
		this.resourceMapper = newResourceMapper(util, client, objectMapper);
		this.includeLookupSetter = newIncludeLookupSetter(resourceRegistry, resourceMapper, propertiesProvider, objectMapper);

		if (serverInfo != null && !serverInfo.isEmpty()) {
			jsonapi = objectMapper.valueToTree(serverInfo);
		}
	}

	public ResourceMapper getResourceMapper() {
		return resourceMapper;
	}

	public void setClient(boolean client) {
		this.client = client;
	}

	protected IncludeLookupSetter newIncludeLookupSetter(ResourceRegistry resourceRegistry, ResourceMapper resourceMapper,
			PropertiesProvider propertiesProvider, ObjectMapper objectMapper) {
		return new IncludeLookupSetter(resourceRegistry, resourceMapper, propertiesProvider, resultFactory, objectMapper);
	}

	protected DocumentMapperUtil newDocumentMapperUtil(ResourceRegistry resourceRegistry, ObjectMapper objectMapper,
			PropertiesProvider propertiesProvider, UrlBuilder urlBuilder) {
		return new DocumentMapperUtil(resourceRegistry, objectMapper, propertiesProvider, urlBuilder);
	}

	protected ResourceMapper newResourceMapper(DocumentMapperUtil util, boolean client, ObjectMapper objectMapper) {
		return new ResourceMapper(util, client, objectMapper, resourceFilterDirectory);
	}


	public Result<Document> toDocument(JsonApiResponse response, QueryAdapter queryAdapter, DocumentMappingConfig
			mappingConfig) {
		if (response == null) {
			LOGGER.debug("null response returned");
			return null;
		}

		int requestVersion = queryAdapter.getQueryContext().getRequestVersion();

		ResourceMappingConfig resourceMapping = mappingConfig.getResourceMapping();

		Document doc = new Document();
		doc.setJsonapi(jsonapi);
		addErrors(doc, response.getErrors());
		util.setMeta(doc, response.getMetaInformation());
		if (mappingConfig.getResourceMapping().getSerializeLinks()) {
			LinksInformation linksInformation = enrichSelfLink(response.getLinksInformation(), queryAdapter);
			util.setLinks(doc, linksInformation, queryAdapter);
		}
		addData(doc, response.getEntity(), queryAdapter, resourceMapping);

		Result<Document> result = addRelationDataAndInclusions(doc, response.getEntity(), queryAdapter, mappingConfig);
		result.doWork(it -> applyIgnoreEmpty(doc, queryAdapter, requestVersion));
		result.doWork(it -> compact(doc, queryAdapter));
		return result;
	}

	private LinksInformation enrichSelfLink(LinksInformation linksInformation, QueryAdapter queryAdapter) {
		if (!queryAdapter.getCompactMode()) {
			QueryContext queryContext = queryAdapter.getQueryContext();
			if (queryContext != null) {
				HttpRequestContext requestContext = queryContext.getRequestContext();
				if (requestContext != null && (linksInformation == null || linksInformation instanceof SelfLinksInformation)) {
					SelfLinksInformation selfLinksInformation = (SelfLinksInformation) linksInformation;
					URI requestUri = requestContext.getRequestUri();
					if ((selfLinksInformation == null || selfLinksInformation.getSelf() == null) && requestUri != null) {
						if (selfLinksInformation == null) {
							selfLinksInformation = new DefaultSelfLinksInformation();
							linksInformation = selfLinksInformation;
						}

						JsonApiUrlBuilder.UrlParameterBuilder urlBuilder = new JsonApiUrlBuilder.UrlParameterBuilder(requestUri.toString());
						urlBuilder.addQueryParameters(requestContext.getRequestParameters());
						selfLinksInformation.setSelf(urlBuilder.toString());
					}
				}
			}
		}
		return linksInformation;
	}

	private void applyIgnoreEmpty(Document doc, QueryAdapter queryAdapter, int requestVersion) {
		if (doc.getData().isPresent()) {
			if (doc.isMultiple()) {
				applyIgnoreEmpty(doc.getCollectionData().get(), requestVersion);
			}
			else {
				applyIgnoreEmpty(doc.getSingleData().get(), requestVersion);
			}
			if (doc.getIncluded() != null) {
				applyIgnoreEmpty(doc.getIncluded(), requestVersion);
			}
		}
	}

	private void applyIgnoreEmpty(List<Resource> resources, int requestVersion) {
		if (resources != null) {
			for (Resource resource : resources) {
				applyIgnoreEmpty(resource, requestVersion);
			}
		}
	}

	private void applyIgnoreEmpty(Resource resource, int requestVersion) {
		String type = resource.getType();
		if (util.hasResourceInformation(type)) {
			ResourceInformation resourceInformation = util.getResourceInformation(type);

			Iterator<Map.Entry<String, Relationship>> iterator = resource.getRelationships().entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Relationship> entry = iterator.next();
				ResourceField field = resourceInformation.findFieldByJsonName(entry.getKey(), requestVersion);
				if (field != null && !isRelationshipIncluded(field, entry.getValue())) {
					iterator.remove();
				}
			}
		}
	}


	private boolean isRelationshipIncluded(ResourceField field, Relationship relationship) {
		JsonIncludeStrategy includeStrategy = field.getJsonIncludeStrategy();
		Nullable<Object> data = relationship.getData();
		return JsonIncludeStrategy.DEFAULT.equals(includeStrategy)
				|| data.isPresent() && data.get() != null && JsonIncludeStrategy.NOT_NULL.equals(includeStrategy)
				|| JsonIncludeStrategy.NON_EMPTY.equals(includeStrategy) && data.isPresent() && !isDefaultRelationshipValue(data.get());
	}

	private boolean isDefaultRelationshipValue(Object o) {
		return o == null || (o instanceof Collection) && ((Collection) o).isEmpty();
	}

	/**
	 * removes unncessary json elements
	 */
	private void compact(Document doc, QueryAdapter queryAdapter) {
		if (queryAdapter != null && queryAdapter.getCompactMode()) {
			if (doc.getIncluded() != null) {
				compact(doc.getIncluded());
			}
			if (doc.getData().isPresent()) {
				if (doc.isMultiple()) {
					compact(doc.getCollectionData().get());
				}
				else {
					compact(doc.getSingleData().get());
				}
			}
		}
	}

	private void compact(List<Resource> resources) {
		if (resources != null) {
			for (Resource resource : resources) {
				compact(resource);
			}
		}
	}

	private void compact(Resource resource) {
		Iterator<Relationship> iterator = resource.getRelationships().values().iterator();
		while (iterator.hasNext()) {
			Relationship rel = iterator.next();
			if (!rel.getData().isPresent()) {
				iterator.remove();
			}
		}

	}

	private Result<Document> addRelationDataAndInclusions(Document doc, Object entity, QueryAdapter queryAdapter,
			DocumentMappingConfig mappingConfig) {

		if (doc.getData().isPresent() && !client) {
			return includeLookupSetter.processInclusions(doc, entity, queryAdapter, mappingConfig);
		}
		else {
			return resultFactory.just(doc);
		}
	}

	private void addData(Document doc, Object entity, QueryAdapter queryAdapter, ResourceMappingConfig resourceMappingConfig) {
		if (entity != null) {
			LOGGER.debug("adding data {}", entity);
			if (entity instanceof Iterable) {
				ArrayList<Object> dataList = new ArrayList<>();
				for (Object obj : (Iterable<?>) entity) {
					dataList.add(resourceMapper.toData(obj, queryAdapter, resourceMappingConfig));
				}
				doc.setData(Nullable.of(dataList));
			}
			else {
				doc.setData(Nullable.of(resourceMapper.toData(entity, queryAdapter, resourceMappingConfig)));
			}
		}
	}

	private void addErrors(Document doc, Iterable<ErrorData> errors) {
		if (errors != null) {
			List<ErrorData> errorList = new ArrayList<>();
			for (ErrorData error : errors) {
				errorList.add(error);
			}
			doc.setErrors(errorList);
			LOGGER.debug("adding errors {}", errorList);
		}
	}

}
