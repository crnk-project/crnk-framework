package io.crnk.core.engine.internal.document.mapper;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;

import java.util.*;

public class IncludeLookupUtil {

	private ResourceRegistry resourceRegistry;


	private IncludeBehavior includeBehavior;

	public IncludeLookupUtil(ResourceRegistry resourceRegistry, IncludeBehavior includeBehavior) {
		this.resourceRegistry = resourceRegistry;
		this.includeBehavior = includeBehavior;
	}


	public static LookupIncludeBehavior getDefaultLookupIncludeBehavior(PropertiesProvider propertiesProvider) {
		if (propertiesProvider == null) {
			return LookupIncludeBehavior.NONE;
		}
		// determine system property for include look up
		String includeAutomaticallyString = propertiesProvider.getProperty(CrnkProperties.INCLUDE_AUTOMATICALLY);
		boolean includeAutomatically = Boolean.parseBoolean(includeAutomaticallyString);
		String includeAutomaticallyOverwriteString =
				propertiesProvider.getProperty(CrnkProperties.INCLUDE_AUTOMATICALLY_OVERWRITE);
		boolean includeAutomaticallyOverwrite = Boolean.parseBoolean(includeAutomaticallyOverwriteString);

		if (includeAutomaticallyOverwrite) {
			return LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
		} else if (includeAutomatically) {
			return LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL;
		}
		return LookupIncludeBehavior.NONE;
	}

	public static IncludeBehavior getIncludeBehavior(PropertiesProvider propertiesProvider) {
		String property = propertiesProvider != null ? propertiesProvider.getProperty(CrnkProperties.INCLUDE_BEHAVIOR) : null;
		if (property == null || property.isEmpty()) {
			return IncludeBehavior.PER_TYPE;
		}
		return IncludeBehavior.valueOf(property.toUpperCase());
	}

	public Set<ResourceField> getRelationshipFields(Collection<Resource> resources) {
		Set<ResourceField> fields = new HashSet<>();

		Set<String> processedTypes = new HashSet<>();

		for (Resource resource : resources) {
			process(resource.getType(), processedTypes, fields);
		}

		return fields;
	}

	private void process(String type, Set<String> processedTypes, Set<ResourceField> fields) {
		if (!processedTypes.contains(type)) {
			processedTypes.add(type);

			RegistryEntry entry = resourceRegistry.getEntry(type);
			ResourceInformation information = entry.getResourceInformation();

			ResourceInformation superInformation = getSuperInformation(information);
			if (superInformation != null) {
				process(superInformation.getResourceType(), processedTypes, fields);
			}

			// TODO same relationship on multiple children
			for (ResourceField field : information.getRelationshipFields()) {
				boolean existsOnSuperType =
						superInformation != null && superInformation.findRelationshipFieldByName(field.getJsonName()) != null;
				if (!existsOnSuperType) {
					fields.add(field);
				}
			}
		}
	}

	private ResourceInformation getSuperInformation(ResourceInformation information) {
		String superclass = information.getSuperResourceType();
		if (superclass == null) {
			return null;
		}
		boolean hasSuperType = resourceRegistry.hasEntry(superclass);
		return hasSuperType ? resourceRegistry.getEntry(superclass).getResourceInformation() : null;
	}

	public List<Resource> filterByType(Collection<Resource> resources, ResourceInformation resourceInformation) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			if (isInstance(resourceInformation, resource)) {
				results.add(resource);
			}
		}
		return results;
	}

	private boolean isInstance(ResourceInformation desiredResourceInformation, Resource resource) {
		if (desiredResourceInformation.getResourceType().equals(resource.getType())) {
			return true;
		}

		// TODO proper ResourceInformation API
		ResourceInformation actualResourceInformation = resourceRegistry.getEntry(resource.getType()).getResourceInformation();
		ResourceInformation superInformation = actualResourceInformation;
		while ((superInformation = getSuperInformation(superInformation)) != null) {
			if (superInformation.equals(desiredResourceInformation)) {
				return true;
			}
		}
		return false;
	}

	public boolean isInclusionRequested(QueryAdapter queryAdapter, List<ResourceField> fieldPath) {
		if (queryAdapter == null || queryAdapter.getIncludedRelations() == null
				|| queryAdapter.getIncludedRelations().getParams() == null) {
			return false;
		}

		if (queryAdapter instanceof QuerySpecAdapter) {
			return isInclusionRequestedForQueryspec(queryAdapter, fieldPath);
		} else {
			return isInclusionRequestedForQueryParams(queryAdapter, fieldPath);
		}
	}

	private boolean isInclusionRequestedForQueryspec(QueryAdapter queryAdapter, List<ResourceField> fieldPath) {
		QuerySpec querySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
		if (includeBehavior == IncludeBehavior.PER_ROOT_PATH) {
			return contains(querySpec, toPathList(fieldPath, 0));
		} else {
			for (int i = fieldPath.size() - 1; i >= 0; i--) {
				List<String> path = toPathList(fieldPath, i);

				// TODO subtyping not properly supported
				ResourceInformation rootInformation = fieldPath.get(i).getParentResourceInformation();
				QuerySpec rootQuerySpec = querySpec.getQuerySpec(rootInformation.getResourceClass());
				if (rootQuerySpec != null && contains(rootQuerySpec, path)) {
					return true;
				}
			}
			return contains(querySpec, toPathList(fieldPath, 0));
		}
	}

	private boolean isInclusionRequestedForQueryParams(QueryAdapter queryAdapter, List<ResourceField> fieldPath) {
		Map<String, IncludedRelationsParams> params = queryAdapter.getIncludedRelations().getParams();

		// we have to possibilities for inclusion: by type or dot notation
		for (int i = fieldPath.size() - 1; i >= 0; i--) {
			String path = toPath(fieldPath, i);
			ResourceInformation rootInformation = fieldPath.get(i).getParentResourceInformation();
			IncludedRelationsParams includedRelationsParams = params.get(rootInformation.getResourceType());
			if (includedRelationsParams != null && contains(includedRelationsParams, path)) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(IncludedRelationsParams includedRelationsParams, String path) {
		String pathPrefix = path + ".";
		for (Inclusion inclusion : includedRelationsParams.getParams()) {
			if (inclusion.getPath().equals(path) || inclusion.getPath().startsWith(pathPrefix)) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(QuerySpec querySpec, List<String> path) {
		for (IncludeRelationSpec inclusion : querySpec.getIncludedRelations()) {
			if (inclusion.getAttributePath().equals(path) || startsWith(inclusion, path)) {
				return true;
			}
		}
		return false;
	}

	private boolean startsWith(IncludeRelationSpec inclusion, List<String> path) {
		return inclusion.getAttributePath().size() > path.size() && inclusion.getAttributePath().subList(0, path.size())
				.equals(path);
	}

	private String toPath(List<ResourceField> fieldPath, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < fieldPath.size(); i++) {
			ResourceField field = fieldPath.get(i);
			if (builder.length() > 0) {
				builder.append(".");
			}
			builder.append(field.getJsonName());
		}
		return builder.toString();
	}

	private List<String> toPathList(List<ResourceField> fieldPath, int offset) {
		List<String> builder = new ArrayList<>();
		List<String> result = builder;
		for (int i = offset; i < fieldPath.size(); i++) {
			ResourceField field = fieldPath.get(i);
			result.add(field.getJsonName());
		}
		return result;
	}

	public List<Resource> sub(Collection<Resource> resourcesWithField, Collection<Resource> resourcesForLookup) {
		List<Resource> result = new ArrayList<>(resourcesWithField);
		result.removeAll(resourcesForLookup);
		return result;
	}

	public List<Resource> filterByLoadedRelationship(List<Resource> resources, ResourceField resourceField) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			if (resource.getRelationships().get(resourceField.getJsonName()) != null) {
				results.add(resource);
			}
		}
		return results;
	}

	public Set<ResourceIdentifier> toIds(Set<Resource> resources) {
		Set<ResourceIdentifier> results = new HashSet<>();
		for (Resource resource : resources) {
			results.add(resource.toIdentifier());
		}
		return results;
	}

	public List<ResourceIdentifier> toIds(List<Resource> resources) {
		List<ResourceIdentifier> results = new ArrayList<>();
		for (Resource resource : resources) {
			results.add(resource.toIdentifier());
		}
		return results;
	}

	public Set<Resource> union(Collection<Resource> set0, Collection<Resource> set1) {
		Map<ResourceIdentifier, Resource> map = new HashMap<>();
		for (Resource resource : set0) {
			map.put(resource.toIdentifier(), resource);
		}
		for (Resource resource : set1) {
			map.put(resource.toIdentifier(), resource);
		}
		return new HashSet<>(map.values());
	}

	public List<Resource> findResourcesWithoutRelationshipData(List<Resource> resources, ResourceField resourceField) {
		List<Resource> results = new ArrayList<>();
		for (Resource resource : resources) {
			Relationship relationship = resource.getRelationships().get(resourceField.getJsonName());
			if (!relationship.getData().isPresent()) {
				results.add(resource);
			}
		}
		return results;
	}
}
