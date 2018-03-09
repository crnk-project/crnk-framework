package io.crnk.core.engine.internal.document.mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentMapper.class);

	private final ResourceFilterDirectory resourceFilterDirectory;

	protected PropertiesProvider propertiesProvider;

	private DocumentMapperUtil util;

	private ResourceMapper resourceMapper;

	private IncludeLookupSetter includeLookupSetter;

	private ResultFactory resultFactory;

	private boolean client;

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider,
						  ResourceFilterDirectory resourceFilterDirectory, ResultFactory resultFactory) {
		this(resourceRegistry, objectMapper, propertiesProvider, resourceFilterDirectory, resultFactory, false);
	}

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider,
						  ResourceFilterDirectory resourceFilterDirectory, ResultFactory resultFactory, boolean client) {
		this.propertiesProvider = propertiesProvider;
		this.client = client;
		this.resultFactory = resultFactory;
		this.resourceFilterDirectory = resourceFilterDirectory;

		PreconditionUtil.assertTrue("filterBehavior necessary on server-side", client || resourceFilterDirectory != null);

		this.util = newDocumentMapperUtil(resourceRegistry, objectMapper, propertiesProvider);
		this.resourceMapper = newResourceMapper(util, client, objectMapper);
		this.includeLookupSetter = newIncludeLookupSetter(resourceRegistry, resourceMapper, propertiesProvider);
	}

	public ResourceFilterDirectory getFilterBehaviorManager() {
		return resourceFilterDirectory;
	}

	protected IncludeLookupSetter newIncludeLookupSetter(ResourceRegistry resourceRegistry, ResourceMapper resourceMapper,
														 PropertiesProvider propertiesProvider) {
		return new IncludeLookupSetter(resourceRegistry, resourceMapper, propertiesProvider, resultFactory);
	}

	protected DocumentMapperUtil newDocumentMapperUtil(ResourceRegistry resourceRegistry, ObjectMapper objectMapper,
													   PropertiesProvider propertiesProvider) {
		return new DocumentMapperUtil(resourceRegistry, objectMapper, propertiesProvider);
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

		ResourceMappingConfig resourceMapping = mappingConfig.getResourceMapping();

		Document doc = new Document();
		addErrors(doc, response.getErrors());
		util.setMeta(doc, response.getMetaInformation());
		if (mappingConfig.getResourceMapping().getSerializeLinks()) {
			util.setLinks(doc, response.getLinksInformation(), queryAdapter);
		}
		addData(doc, response.getEntity(), queryAdapter, resourceMapping);

		Result<Document> result = addRelationDataAndInclusions(doc, response.getEntity(), queryAdapter, mappingConfig);
		result.doWork(it -> compact(doc, queryAdapter));
		return result;
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
			} else {
				compact(doc.getSingleData().get());
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
		} else {
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
				doc.setData(Nullable.of((Object) dataList));
			} else {
				doc.setData(Nullable.of((Object) resourceMapper.toData(entity, queryAdapter, resourceMappingConfig)));
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
