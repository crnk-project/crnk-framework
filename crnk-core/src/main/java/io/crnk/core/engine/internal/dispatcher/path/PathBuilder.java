package io.crnk.core.engine.internal.dispatcher.path;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.exception.ResourceFieldNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Builder responsible for parsing URL path.
 */
public class PathBuilder {
	public static final String SEPARATOR = "/";
	public static final String RELATIONSHIP_MARK = "relationships";

	private final ResourceRegistry resourceRegistry;

	public PathBuilder(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	private static PathIds createPathIds(String idsString) {
		List<String> pathIds = Arrays.asList(idsString.split(PathIds.ID_SEPARATOR_PATTERN));
		return new PathIds(pathIds);
	}

	private static String[] splitPath(String path) {
		if (path.startsWith(SEPARATOR)) {
			path = path.substring(1);
		}
		if (path.endsWith(SEPARATOR)) {
			path = path.substring(0, path.length());
		}
		return path.split(SEPARATOR);
	}

	/**
	 * Creates a path using the provided JsonPath structure.
	 *
	 * @param jsonPath JsonPath structure to be parsed
	 * @return String representing structure provided in the input
	 */
	public static String build(JsonPath jsonPath) {
		List<String> urlParts = new LinkedList<>();

		JsonPath currentJsonPath = jsonPath;
		String pathPart;
		do {
			if (currentJsonPath instanceof RelationshipsPath) {
				pathPart = RELATIONSHIP_MARK + SEPARATOR + currentJsonPath.getElementName();
			} else if (currentJsonPath instanceof FieldPath) {
				pathPart = currentJsonPath.getElementName();
			} else {
				pathPart = currentJsonPath.getElementName();
				if (currentJsonPath.getIds() != null) {
					pathPart += SEPARATOR + mergeIds(currentJsonPath.getIds());
				}
			}
			urlParts.add(pathPart);

			currentJsonPath = currentJsonPath.getParentResource();
		} while (currentJsonPath != null);
		Collections.reverse(urlParts);

		return SEPARATOR + StringUtils.join(SEPARATOR, urlParts) + SEPARATOR;
	}

	private static String mergeIds(PathIds ids) {
		return StringUtils.join(PathIds.ID_SEPARATOR, ids.getIds());
	}

	/**
	 * Parses path provided by the application. The path provided cannot contain neither hostname nor protocol. It
	 * can start or end with slash e.g. <i>/tasks/1/</i> or <i>tasks/1</i>.
	 *
	 * @param path Path to be parsed
	 * @return doubly-linked list which represents path given at the input
	 */
	public JsonPath build(String path) {
		String[] strings = splitPath(path);
		if (strings.length == 0 || (strings.length == 1 && "".equals(strings[0]))) {
			throw new ResourceException("Path is empty");
		}

		JsonPath previousJsonPath = null, currentJsonPath = null;
		PathIds pathIds;
		boolean relationshipMark;
		String elementName;
		String actionName;

		int currentElementIdx = 0;
		while (currentElementIdx < strings.length) {
			elementName = null;
			pathIds = null;
			actionName = null;
			relationshipMark = false;

			if (RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
				relationshipMark = true;
				currentElementIdx++;
			}

			RegistryEntry entry = null;
			if (currentElementIdx < strings.length && !RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
				elementName = strings[currentElementIdx];

				// support "/" in resource type to group repositories
				StringBuilder potentialResourceType = new StringBuilder();
				for (int i = 0; currentElementIdx + i < strings.length; i++) {
					if (potentialResourceType.length() > 0) {
						potentialResourceType.append("/");
					}
					potentialResourceType.append(strings[currentElementIdx + i]);
					entry = resourceRegistry.getEntry(potentialResourceType.toString());
					if (entry != null) {
						currentElementIdx += i;
						elementName = potentialResourceType.toString();
						break;
					}
				}

				currentElementIdx++;
			}

			if (currentElementIdx < strings.length && entry != null && entry.getRepositoryInformation().getActions().containsKey(strings[currentElementIdx])) {
				// repository action
				actionName = strings[currentElementIdx];
				currentElementIdx++;
			} else if (currentElementIdx < strings.length && !RELATIONSHIP_MARK.equals(strings[currentElementIdx])) {
				// ids
				pathIds = createPathIds(strings[currentElementIdx]);
				currentElementIdx++;

				if (currentElementIdx < strings.length && entry != null && entry.getRepositoryInformation().getActions().containsKey(strings[currentElementIdx])) {
					// resource action
					actionName = strings[currentElementIdx];
					currentElementIdx++;
				}
			}

			if (previousJsonPath != null) {
				currentJsonPath = getNonResourcePath(previousJsonPath, elementName, relationshipMark);
				if (pathIds != null) {
					throw new ResourceException("RelationshipsPath and FieldPath cannot contain ids");
				}
			} else if (entry != null && !relationshipMark) {
				currentJsonPath = new ResourcePath(elementName);
			} else {
				return null;
			}

			if (pathIds != null) {
				currentJsonPath.setIds(pathIds);
			}
			if (actionName != null) {
				ActionPath actionPath = new ActionPath(actionName);
				actionPath.setParentResource(currentJsonPath);
				currentJsonPath.setChildResource(actionPath);
				currentJsonPath = actionPath;
			}
			if (previousJsonPath != null) {
				previousJsonPath.setChildResource(currentJsonPath);
				currentJsonPath.setParentResource(previousJsonPath);
			}
			previousJsonPath = currentJsonPath;
		}

		return currentJsonPath;
	}

	private JsonPath getNonResourcePath(JsonPath previousJsonPath, String elementName, boolean relationshipMark) {
		String previousElementName = previousJsonPath.getElementName();
		RegistryEntry previousEntry = resourceRegistry.getEntry(previousElementName);

		ResourceInformation resourceInformation = previousEntry.getResourceInformation();

		List<ResourceField> resourceFields = resourceInformation.getRelationshipFields();
		for (ResourceField field : resourceFields) {
			if (field.getJsonName().equals(elementName)) {
				if (relationshipMark) {
					return new RelationshipsPath(elementName);
				} else {
					return new FieldPath(elementName);
				}
			}
		}
		//TODO: Throw different exception? element name can be null..
		throw new ResourceFieldNotFoundException(elementName);
	}
}