package io.crnk.data.facet.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        for (FacetResourceInformation facetResourceInformation : config.getResources().values()) {
            String resourceType = facetResourceInformation.getType();
            if (!isFiltered(resourceType)) {
                List<FacetResource> typeFacets = new ArrayList<>();

                Collection<FacetInformation> facetInformations = facetResourceInformation.getFacets().values();
                facetInformations = applyQuickFilter(facetInformations, querySpec);
                facetInformations = orderFacets(facetInformations, querySpec);

                // to be refined with each further (selected) facet
                QuerySpec facetQuerySpec = new QuerySpec(facetResourceInformation.getType());

                // iterate over facets of current type
                for (FacetInformation facetInformation : facetInformations) {
                    FacetResource facet = computeFacet(facetInformation, facetQuerySpec);
                    updateNestedFacetFilter(facet, facetQuerySpec, querySpec);
                    typeFacets.add(facet);
                }
                facets.addAll(typeFacets);
            }
        }

        return removeSelections(querySpec).apply(facets);
    }

    private FacetResource computeFacet(FacetInformation facetInformation, QuerySpec facetQuerySpec) {
        FacetResourceInformation facetResourceInformation = facetInformation.getResource();


        FacetProvider provider = facetResourceInformation.getProvider();
        List<FacetValue> values = provider.findValues(facetInformation, facetQuerySpec);

        FacetResource facet = new FacetResource();
        facet.setType(facetResourceInformation.getType());
        facet.setName(facetInformation.getName());
        facet.setId(facet.getType() + "_" + facet.getName());
        facet.setValues(values.stream().collect(Collectors.toMap(FacetValue::getLabel, it -> it)));
        facet.setLabels(values.stream().map(it -> it.getLabel()).collect(Collectors.toList()));
        return facet;
    }

    private QuerySpec removeSelections(QuerySpec querySpec) {
        QuerySpec result = querySpec.clone();
        Iterator<FilterSpec> iterator = result.getFilters().iterator();
        while (iterator.hasNext()) {
            FilterSpec filterSpec = iterator.next();
            if (filterSpec.getOperator() == FilterOperator.SELECT) {
                iterator.remove();
            }
        }
        return result;
    }

    private void updateNestedFacetFilter(FacetResource facet, QuerySpec nestedFacetFilter, QuerySpec querySpec) {
        // check whether a filter has been apply to this facet
        PathSpec selectionPath = PathSpec.of(FacetResource.ATTR_VALUES, facet.getName());
        Optional<FilterSpec> optSelection = querySpec.findFilter(selectionPath);
        if (optSelection.isPresent()) {
            FilterSpec selectionSpec = optSelection.get();
            PreconditionUtil.assertEquals("expected EQ", FilterOperator.SELECT, selectionSpec.getOperator());
            FilterSpec selectedFilterSpec = getFilterSpec(facet, selectionSpec.getValue());
            if (selectedFilterSpec != null) {
                nestedFacetFilter.addFilter(selectedFilterSpec);
            }
        }
    }

    private FilterSpec getFilterSpec(FacetResource facet, Object facetLabels) {
        FilterSpec selectedFilterSpec = null;
        Collection<String> selectedLabels = facetLabels instanceof Collection ? (Collection<String>) facetLabels : Collections.singleton((String) facetLabels);
        for (String selectedLabel : selectedLabels) {
            FacetValue selectedValue = facet.getValues().get(selectedLabel);
            if (selectedValue != null && selectedFilterSpec == null) {
                selectedFilterSpec = selectedValue.getFilterSpec();
            } else if (selectedValue != null) {
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
        } else {
            values.add(value);
        }
    }

    /**
     * @return facet informations ordered as requested through filter[name]
     */
    private Collection<FacetInformation> orderFacets(Collection<FacetInformation> facetInformations, QuerySpec querySpec) {
        Optional<FilterSpec> nameFilter = querySpec.findFilter(PathSpec.of(FacetResource.ATTR_NAME));
        if (nameFilter.isPresent() && facetInformations.size() > 1) {
            PreconditionUtil.assertTrue(nameFilter.get().getValue() instanceof List, "expected list of filter values");
            List<String> facetNames = nameFilter.get().getValue();

            List<FacetInformation> orderedInformations = new ArrayList<>(facetInformations);
            Collections.sort(orderedInformations, (o1, o2) -> {
                int i1 = facetNames.indexOf(o1.getName());
                int i2 = facetNames.indexOf(o2.getName());
                PreconditionUtil.assertTrue("expected to find", i1 != -1);
                PreconditionUtil.assertTrue("expected to find", i2 != -1);
                return i1 - i2;
            });
            return orderedInformations;
        }
        return facetInformations;
    }

    private Collection<FacetInformation> applyQuickFilter(Collection<FacetInformation> facetInformations, QuerySpec querySpec) {
        Optional<FilterSpec> typeFilter = querySpec.findFilter(PathSpec.of(FacetResource.ATTR_TYPE));
        Optional<FilterSpec> nameFilter = querySpec.findFilter(PathSpec.of(FacetResource.ATTR_NAME));

        InMemoryEvaluator evaluator = new InMemoryEvaluator();
        return facetInformations.stream()
                .filter(it -> !nameFilter.isPresent() || evaluator.matchesFilter(it, nameFilter.get()))
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
