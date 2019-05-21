package io.crnk.data.facet.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.InMemoryEvaluator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.FacetModuleConfig;
import io.crnk.data.facet.FacetRepository;
import io.crnk.data.facet.FacetResource;
import io.crnk.data.facet.FacetValue;
import io.crnk.data.facet.config.FacetInformation;
import io.crnk.data.facet.config.FacetResourceInformation;
import io.crnk.data.facet.provider.FacetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetRepositoryImpl extends ReadOnlyResourceRepositoryBase<FacetResource, String> implements FacetRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacetRepositoryImpl.class);

	private final Runnable initializer;

	private final Module.ModuleContext moduleContext;

	private FacetModuleConfig config;

	public FacetRepositoryImpl(FacetModuleConfig config, Module.ModuleContext moduleContext, Runnable initializer) {
		super(FacetResource.class);
		this.config = config;
		this.initializer = initializer;
		this.moduleContext = moduleContext;
	}


	@Override
	public ResourceList<FacetResource> findAll(QuerySpec querySpec) {
		initializer.run();

		List<FacetResource> facets = new ArrayList<>();

		List<String> groupNames = getGroupNames(querySpec);

		for (FacetResourceInformation facetResourceInformation : config.getResources().values()) {
			String resourceType = facetResourceInformation.getType();
			if (!isFiltered(resourceType) && hasGroups(facetResourceInformation, groupNames)) {
				List<FacetInformation> facetInformations = new ArrayList<>(facetResourceInformation.getFacets().values());
				facetInformations = applyQuickFilter(facetInformations, querySpec, groupNames);
				facetInformations = orderFacets(facetInformations, querySpec, groupNames);

				// iterate over facets of current type
				QuerySpec facetQuerySpec = new QuerySpec(facetResourceInformation.getType());
				List<FacetResource> results = new ArrayList<>();
				if (!facetInformations.isEmpty()) {
					computeFacet(results, facetQuerySpec, querySpec, facetInformations, 0, null, groupNames, resourceType);
				}
				facets.addAll(results);
			}
		}

		return removeFacetOperators(querySpec).apply(facets);
	}

	private boolean hasGroups(FacetResourceInformation facetResourceInformation, List<String> groupNames) {
		for (String groupName : groupNames) {
			if (!facetResourceInformation.getFacets().containsKey(groupName)) {
				return false;
			}
		}
		return true;
	}

	private List<String> getGroupNames(QuerySpec querySpec) {
		Optional<FilterSpec> optGrouping = querySpec.findFilter(PathSpec.of("values"), FilterOperator.GROUP);
		List<String> groupNames = optGrouping.isPresent() ? toList(optGrouping.get().getValue()) : Collections.emptyList();
		for (String groupName : groupNames) {
			if (!existsFacet(groupName)) {
				throw new BadRequestException("no facet found with name=" + groupName + " to group by");
			}
		}
		return groupNames;
	}

	private boolean existsFacet(String groupName) {
		return config.getResources().values().stream().filter(it -> it.getFacets().containsKey(groupName)).findFirst().isPresent();
	}

	private List<String> toList(Object value) {
		ArrayList set = new ArrayList<>();
		if (value instanceof Collection) {
			set.addAll(((Collection) value));
		}
		else {
			set.add(value);
		}
		return set;
	}

	private void computeFacet(List<FacetResource> results, QuerySpec facetQuerySpec, QuerySpec querySpec, List<FacetInformation> facetInformations, int index, FacetResource previousFacet,
			List<String> groupNames, String idPrefix) {
		FacetInformation facetInformation = facetInformations.get(index);
		FacetResourceInformation facetResourceInformation = facetInformation.getResource();

		FacetProvider provider = facetResourceInformation.getProvider();
		List<FacetValue> values = new ArrayList<>(provider.findValues(facetInformation, facetQuerySpec));

		if (groupNames.contains(facetInformation.getName())) {
			// create facet for every group value
			// TODO consider providing optimized support for this with SQL GROUP BY statements, etc.
			List<FacetResource> facets = new ArrayList<>();
			for (FacetValue value : values) {
				String label = value.getLabel();
				Map<String, FacetValue> valueMap = new HashMap<>();
				valueMap.put(label, value);
				FacetResource facet = createFacetResource(facetInformation);
				facet.setId(idPrefix + "_" + facet.getName() + "_" + label);
				facet.setValues(valueMap);
				facet.setLabels(Arrays.asList(label));
				facet.getGroups().put(facetInformation.getName(), label);
				if (previousFacet != null) {
					facet.getGroups().putAll(previousFacet.getGroups());
				}
				results.add(facet);
				facets.add(facet);
			}
			for (int i = 0; i < values.size(); i++) {
				if (index < facetInformations.size() - 1) {
					FacetResource facet = facets.get(i);
					FacetValue value = values.get(i);
					QuerySpec groupQuerySpec = facetQuerySpec.clone();
					groupQuerySpec.addFilter(value.getFilterSpec());
					computeFacet(results, groupQuerySpec, querySpec, facetInformations, index + 1, facet, groupNames, facet.getId());
				}
			}
		}
		else {
			// create a regular single facet
			FacetResource facet = createFacetResource(facetInformation);
			facet.setId(idPrefix + "_" + facet.getName());
			facet.setValues(values.stream().collect(Collectors.toMap(FacetValue::getLabel, it -> it)));
			facet.setLabels(values.stream().map(it -> it.getLabel()).collect(Collectors.toList()));
			if (previousFacet != null) {
				facet.getGroups().putAll(previousFacet.getGroups());
			}
			results.add(facet);
			if (index < facetInformations.size() - 1) {
				updateNestedFacetFilter(facet, facetQuerySpec, querySpec);
				computeFacet(results, facetQuerySpec, querySpec, facetInformations, index + 1, facet, groupNames, idPrefix);
			}
		}
	}


	private FacetResource createFacetResource(FacetInformation facetInformation) {
		FacetResourceInformation facetResourceInformation = facetInformation.getResource();
		FacetResource facet = new FacetResource();
		facet.setResourceType(facetResourceInformation.getType());
		facet.setName(facetInformation.getName());
		facet.setId(facet.getResourceType() + "_" + facet.getName());
		return facet;
	}

	private void updateNestedFacetFilter(FacetResource facet, QuerySpec nestedFacetFilter, QuerySpec querySpec) {
		// check whether a filter has been apply to this facet
		PathSpec selectionPath = PathSpec.of(FacetResource.ATTR_VALUES, facet.getName());
		Optional<FilterSpec> optSelection = querySpec.findFilter(selectionPath, FilterOperator.SELECT);
		if (optSelection.isPresent()) {
			FilterSpec selectionSpec = optSelection.get();
			FilterSpec selectedFilterSpec = getFilterSpec(facet, selectionSpec.getValue());
			if (selectedFilterSpec != null) {
				nestedFacetFilter.addFilter(selectedFilterSpec);
			}
		}
	}

	private QuerySpec removeFacetOperators(QuerySpec querySpec) {
		QuerySpec result = querySpec.clone();
		Iterator<FilterSpec> iterator = result.getFilters().iterator();
		while (iterator.hasNext()) {
			FilterSpec filterSpec = iterator.next();
			if (filterSpec.getOperator() == FilterOperator.SELECT || filterSpec.getOperator() == FilterOperator.GROUP) {
				iterator.remove();
			}
		}
		return result;
	}

	private FilterSpec getFilterSpec(FacetResource facet, Object facetLabels) {
		FilterSpec selectedFilterSpec = null;
		Collection<String> selectedLabels = facetLabels instanceof Collection ? (Collection<String>) facetLabels : Collections.singleton((String) facetLabels);
		for (String selectedLabel : selectedLabels) {
			FacetValue selectedValue = facet.getValues().get(selectedLabel);
			if (selectedValue != null && selectedFilterSpec == null) {
				selectedFilterSpec = selectedValue.getFilterSpec();
			}
			else if (selectedValue != null) {
				selectedFilterSpec = mergedAnd(selectedFilterSpec, selectedValue.getFilterSpec());
			}
		}
		return selectedFilterSpec;
	}

	private FilterSpec mergedAnd(FilterSpec spec0, FilterSpec spec1) {
		if (Objects.equals(spec0.getOperator(), spec1.getOperator()) && Objects.equals(spec0.getPath(), spec1.getPath())) {
			return new FilterSpec(spec0.getPath(), spec0.getOperator(), mergeValues(spec0.getValue(), spec1.getValue()));
		}
		return FilterSpec.and(spec0, spec1);
	}

	private Object mergeValues(Object value0, Object value1) {
		Set values = new HashSet();
		addAll(values, value0);
		addAll(values, value1);
		return values;
	}

	private void addAll(Set values, Object value) {
		if (value instanceof Collection) {
			values.addAll((Collection) value);
		}
		else {
			values.add(value);
		}
	}

	/**
	 * @return facet informations ordered as requested through filter[name]
	 */
	private List<FacetInformation> orderFacets(List<FacetInformation> facetInformations, QuerySpec querySpec, List<String> groupNames) {
		Optional<FilterSpec> nameFilter = querySpec.findFilter(PathSpec.of(FacetResource.ATTR_NAME));
		List<String> facetNames = nameFilter.isPresent() ? toList(nameFilter.get().getValue()) : Collections.emptyList();

		List<FacetInformation> orderedInformations = new ArrayList<>(facetInformations);
		Collections.sort(orderedInformations, (o1, o2) -> {
			// groups always come first since other facets are clustered accordingly
			int g1 = indexOrEnd(groupNames, o1.getName());
			int g2 = indexOrEnd(groupNames, o2.getName());
			if (g1 != g2) {
				return g1 - g2;
			}

			// allow to customize default facet order
			int i1 = indexOrEnd(facetNames, o1.getName());
			int i2 = indexOrEnd(facetNames, o2.getName());
			if (i1 != i2) {
				return i1 - i2;
			}

			// natural order of fields on resource
			i1 = facetInformations.indexOf(o1);
			i2 = facetInformations.indexOf(o2);
			return i1 - i2;
		});
		return orderedInformations;
	}

	private int indexOrEnd(List<String> list, String entry) {
		int index = list.indexOf(entry);
		return index != -1 ? index : (Integer.MAX_VALUE >> 2);
	}

	private List<FacetInformation> applyQuickFilter(List<FacetInformation> facetInformations, QuerySpec querySpec, List<String> groupNames) {
		Optional<FilterSpec> typeFilter = querySpec.findFilter(PathSpec.of(FacetResource.ATTR_TYPE));
		Optional<FilterSpec> nameFilter = querySpec.findFilter(PathSpec.of(FacetResource.ATTR_NAME));

		InMemoryEvaluator evaluator = new InMemoryEvaluator();
		return facetInformations.stream()
				.filter(it -> groupNames.contains(it.getName()) || !nameFilter.isPresent() || evaluator.matchesFilter(it, nameFilter.get()))
				.filter(it -> !typeFilter.isPresent() || evaluator.matchesFilter(it.getResource(), typeFilter.get()))
				.collect(Collectors.toList());

	}

	private boolean isFiltered(String resourceType) {
		RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(resourceType);
		ResourceFilterDirectory filterDirectory = moduleContext.getResourceFilterDirectory();
		ResourceInformation resourceInformation = entry.getResourceInformation();
		FilterBehavior filterBehavior = filterDirectory.get(resourceInformation, HttpMethod.GET, new QueryContext());
		boolean filtered = filterBehavior != FilterBehavior.NONE;
		if (filtered) {
			LOGGER.debug("not authorized to access facet of {}", resourceType);
		}
		return filtered;
	}
}
