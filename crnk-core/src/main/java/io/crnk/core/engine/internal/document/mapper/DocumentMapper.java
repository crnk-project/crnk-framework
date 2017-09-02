package io.crnk.core.engine.internal.document.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DocumentMapper {

	private final ResourceFilterDirectory resourceFilterDirectory;

	protected PropertiesProvider propertiesProvider;
	private DocumentMapperUtil util;
	private ResourceMapper resourceMapper;
	private IncludeLookupSetter includeLookupSetter;
	private boolean client;

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider, ResourceFilterDirectory resourceFilterDirectory) {
		this(resourceRegistry, objectMapper, propertiesProvider, resourceFilterDirectory, false);
	}

	public DocumentMapper(ResourceRegistry resourceRegistry, ObjectMapper objectMapper, PropertiesProvider propertiesProvider, ResourceFilterDirectory resourceFilterDirectory, boolean client) {
		this.propertiesProvider = propertiesProvider;
		this.client = client;
		this.resourceFilterDirectory = resourceFilterDirectory;

		PreconditionUtil.assertTrue("filterBehavior necessary on server-side", client || resourceFilterDirectory != null);

		this.util = new DocumentMapperUtil(resourceRegistry, objectMapper);
		this.resourceMapper = newResourceMapper(util, client, objectMapper);
		this.includeLookupSetter = new IncludeLookupSetter(resourceRegistry, resourceMapper, propertiesProvider);
	}

	public ResourceFilterDirectory getFilterBehaviorManager(){
		return resourceFilterDirectory;
	}

	protected ResourceMapper newResourceMapper(DocumentMapperUtil util, boolean client, ObjectMapper objectMapper) {
		return new ResourceMapper(util, client, objectMapper, resourceFilterDirectory);
	}

	public Document toDocument(JsonApiResponse response, QueryAdapter queryAdapter) {
		return toDocument(response, queryAdapter, null);
	}

	public Document toDocument(JsonApiResponse response, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider) {
		Set<String> eagerLoadedRelations = Collections.emptySet();
		return toDocument(response, queryAdapter, parameterProvider, eagerLoadedRelations);
	}

	public Document toDocument(JsonApiResponse response, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Set<String> additionalEagerLoadedRelations) {
		if (response == null) {
			return null;
		}

		Document doc = new Document();
		addErrors(doc, response.getErrors());
		util.setMeta(doc, response.getMetaInformation());
		util.setLinks(doc, response.getLinksInformation());
		addData(doc, response.getEntity(), queryAdapter);
		addRelationDataAndInclusions(doc, response.getEntity(), queryAdapter, parameterProvider, additionalEagerLoadedRelations);

		return doc;
	}

	private void addRelationDataAndInclusions(Document doc, Object entity, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Set<String> additionalEagerLoadedRelations) {
		if (doc.getData().isPresent() && !client) {
			includeLookupSetter.setIncludedElements(doc, entity, queryAdapter, parameterProvider, additionalEagerLoadedRelations);
		}
	}

	private void addData(Document doc, Object entity, QueryAdapter queryAdapter) {
		if (entity != null) {
			if (entity instanceof Iterable) {
				ArrayList<Object> dataList = new ArrayList<>();
				for (Object obj : (Iterable<?>) entity) {
					dataList.add(resourceMapper.toData(obj, queryAdapter));
				}
				doc.setData(Nullable.of((Object) dataList));
			} else {
				doc.setData(Nullable.of((Object) resourceMapper.toData(entity, queryAdapter)));
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
		}
	}

}
