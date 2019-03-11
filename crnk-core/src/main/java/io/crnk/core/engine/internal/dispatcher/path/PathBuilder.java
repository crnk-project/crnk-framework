package io.crnk.core.engine.internal.dispatcher.path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder responsible for parsing URL path.
 */
public class PathBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(PathBuilder.class);

	public static final String SEPARATOR = "/";

	public static final String RELATIONSHIP_MARK = "relationships";

	private final ResourceRegistry resourceRegistry;

	private final TypeParser parser;

	public PathBuilder(ResourceRegistry resourceRegistry, TypeParser parser) {
		this.resourceRegistry = resourceRegistry;
		this.parser = parser;
	}

	private static List<Serializable> parseIds(String idsString, ResourceInformation resourceInformation) {
		String[] strPathIds = idsString.split(JsonPath.ID_SEPARATOR_PATTERN);

		List<Serializable> pathIds = new ArrayList<>();
		for (String strPathId : strPathIds) {
			pathIds.add(resourceInformation.parseIdString(strPathId));
		}

		return pathIds;
	}

	private List<Serializable> parseNestedIds(String idsString, Serializable parentId, ResourceField parentField) {
		String[] strPathIds = idsString.split(JsonPath.ID_SEPARATOR_PATTERN);

		ResourceInformation resourceInformation = parentField.getResourceInformation();
		Class<?> idType = resourceInformation.getIdField().getType();

		ResourceFieldAccessor nestedIdAccessor = resourceInformation.getChildIdAccessor();
		ResourceFieldAccessor parentIdAccessor = resourceInformation.getParentIdAccessor();

		List<Serializable> pathIds = new ArrayList<>();
		for (String strPathId : strPathIds) {
			Serializable nestedId = (Serializable) ClassUtils.newInstance(idType);
			Object childId = parser.parse(strPathId, nestedIdAccessor.getImplementationClass());
			parentIdAccessor.setValue(nestedId, parentId);
			nestedIdAccessor.setValue(nestedId, childId);
			pathIds.add(nestedId);
		}

		return pathIds;
	}

	private static String[] splitPath(String path) {
		if (path.startsWith(SEPARATOR)) {
			path = path.substring(1);
		}
		if (path.endsWith(SEPARATOR)) {
			path = path;
		}
		return path.split(SEPARATOR);
	}

	/**
	 * Parses path provided by the application. The path provided cannot contain neither hostname nor protocol. It
	 * can start or end with slash e.g. <i>/tasks/1/</i> or <i>tasks/1</i>.
	 *
	 * @param path Path to be parsed
	 * @return doubly-linked list which represents path given at the input
	 */
	public JsonPath build(String path) {
		String[] pathElements = splitPath(path);
		if (pathElements.length == 0 || (pathElements.length == 1 && "".equals(pathElements[0]))) {
			return null;
		}
		return parseResourcePath(new LinkedList<>(Arrays.asList(pathElements)));
	}

	private JsonPath parseResourcePath(LinkedList<String> pathElements) {
		RegistryEntry rootEntry = getRootEntry(pathElements);
		if (rootEntry == null) {
			return null;
		}
		if (pathElements.isEmpty()) {
			return new ResourcePath(rootEntry, null);
		}
		Map<String, RepositoryAction> actions = rootEntry.getRepositoryInformation().getActions();
		if (actions.containsKey(pathElements.peek())) {
			String pathElement = pathElements.pop();
			return new ActionPath(rootEntry, null, pathElement);
		}
		return parseIdPath(rootEntry, pathElements);
	}

	private JsonPath parseIdPath(RegistryEntry entry, LinkedList<String> pathElements) {
		String pathElement = pathElements.pop();
		List<Serializable> ids = parseIds(pathElement, entry.getResourceInformation());
		return parseFieldPath(entry, ids, pathElements);
	}

	private JsonPath parseFieldPath(RegistryEntry entry, List<Serializable> ids, LinkedList<String> pathElements) {
		if (pathElements.isEmpty()) {
			return new ResourcePath(entry, ids);
		}

		Map<String, RepositoryAction> actions = entry.getRepositoryInformation().getActions();
		if (actions.containsKey(pathElements.peek())) {
			String pathElement = pathElements.pop();
			return new ActionPath(entry, ids, pathElement);
		}

		String fieldName = pathElements.pop();

		if (fieldName.equals(RELATIONSHIP_MARK)) {
			if (pathElements.isEmpty()) {
				throw new BadRequestException("invalid url, relationships fragment must be followed by name");
			}
			fieldName = pathElements.poll();

			ResourceField field = entry.getResourceInformation().findRelationshipFieldByName(fieldName);
			if (field == null) {
				throw new BadRequestException("invalid url, requested field not found: " + fieldName);
			}

			if (!pathElements.isEmpty()) {
				throw new BadRequestException("invalid url, cannot add further url fragments after relationship name");
			}

			return new RelationshipsPath(entry, ids, field);
		}
		ResourceField field = entry.getResourceInformation().findFieldByName(fieldName);
		if (field == null) {
			throw new BadRequestException("field not found: " + fieldName);
		}
		if (pathElements.isEmpty()) {
			if (isNestedField(field) && !field.isCollection()) {
				RegistryEntry nestedEntry = getNestedEntry(field);
				ResourcePath path = new ResourcePath(nestedEntry, ids);
				path.addParentField(field);
				return path;
			}
			return new FieldPath(entry, ids, field);
		}

		if (field.getResourceFieldType() != ResourceFieldType.RELATIONSHIP || field.getOppositeName() == null) {
			LOGGER.debug("cannot process field={} is not a relationship with an opposite field", field);
			throw new BadRequestException("invalid url, cannot add further url fragements after field");
		}

		RegistryEntry oppositeEntry = resourceRegistry.getEntry(field.getOppositeResourceType());
		ResourceInformation oppositeInformation = oppositeEntry.getResourceInformation();
		ResourceField oppositeField = oppositeInformation.findRelationshipFieldByName(field.getOppositeName());
		if (!oppositeInformation.isNested()) {
			LOGGER.debug("cannot process field={} because opposite={} is not an nested resource", field, oppositeInformation);
			throw new BadRequestException("invalid url, cannot specify ID of related resource");
		}

		PreconditionUtil.verify(oppositeField != null, "nested resource must specify opposite on relationship from parent to child, got null for %s", field);

		List<Serializable> nestedIds;
		if (field.isCollection()) {
			PreconditionUtil.verify(ids.size() == 1, "cannot follow multiple ids along nested path");
			Serializable parentId = ids.get(0);

			// nested many-relationship must specify a nested id
			String strNestedId = pathElements.poll();
			nestedIds = parseNestedIds(strNestedId, parentId, oppositeField);
		}
		else {
			nestedIds = ids;
		}

		JsonPath jsonPath = parseFieldPath(oppositeEntry, nestedIds, pathElements);
		if (jsonPath != null) {
			jsonPath.addParentField(field);
		}
		return jsonPath;
	}

	private boolean isNestedField(ResourceField field) {
		if (field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP) {
			RegistryEntry oppositeType = resourceRegistry.getEntry(field.getOppositeResourceType());
			PreconditionUtil.verify(oppositeType != null, "opposite type %s not found for %s", field.getOppositeResourceType(), field.getUnderlyingName());
			return oppositeType.getResourceInformation().isNested();
		}
		return false;
	}

	private RegistryEntry getNestedEntry(ResourceField field) {
		PreconditionUtil.verify(field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP, "not a relationship");
		RegistryEntry oppositeType = resourceRegistry.getEntry(field.getOppositeResourceType());
		ResourceInformation resourceInformation = oppositeType.getResourceInformation();
		PreconditionUtil.verify(resourceInformation.isNested(), "not a nested relationship");
		return oppositeType;
	}

	private RegistryEntry getRootEntry(LinkedList<String> pathElements) {
		StringBuilder potentialResourcePath = new StringBuilder(pathElements.pop());
		while (true) {
			RegistryEntry entry = getEntryByPath(potentialResourcePath.toString());
			if (entry != null) {
				return entry;
			}

			if (pathElements.isEmpty() || pathElements.peek().equals(RELATIONSHIP_MARK)) {
				break;
			}

			String pathElement = pathElements.pop();

			if (potentialResourcePath.length() > 0) {
				potentialResourcePath.append("/");
			}
			potentialResourcePath.append(pathElement);
		}

		return null;
	}


	private RegistryEntry getEntryByPath(String path) {
		RegistryEntry entry = resourceRegistry.getEntryByPath(path);
		if (entry != null) {
			ResourceRepositoryInformation repositoryInformation = entry.getRepositoryInformation();
			if (repositoryInformation == null || !repositoryInformation.isExposed()) {
				return null;
			}
		}
		return entry;
	}
}