package io.crnk.data.facet.provider;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Prioritizable;
import io.crnk.data.facet.FacetValue;
import io.crnk.data.facet.config.BasicFacetInformation;
import io.crnk.data.facet.config.FacetInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryFacetProvider extends FacetProviderBase implements Prioritizable {

	@Override
	public boolean accepts(RegistryEntry entry) {
		return true;
	}

	@Override
	public List<FacetValue> findValues(FacetInformation facetInformation, QuerySpec querySpec) {
		String resourceType = facetInformation.getResource().getType();
		ResourceRepository repository = context.getRepository(resourceType);
		ResourceInformation resourceInformation = context.getResourceInformation(resourceType);

		if (facetInformation instanceof BasicFacetInformation) {
			BasicFacetInformation basicFacetInformation = (BasicFacetInformation) facetInformation;

			PathSpec path = basicFacetInformation.getPath();
			if (path.getElements().size() > 1) {
				throw new IllegalStateException("not yet supported");
			}
			String pathElement = path.getElements().get(0);
			ResourceField field = resourceInformation.findFieldByUnderlyingName(pathElement);
			ResourceFieldAccessor accessor = field.getAccessor();

			TypeParser typeParser = context.getTypeParser();
			Map<Object, FacetValue> facetValueMap = new HashMap<>();

			ResourceList list = repository.findAll(querySpec);
			for (Object resource : list) {
				Object value = accessor.getValue(resource);

				FacetValue facetValue = facetValueMap.get(value);
				if (facetValue == null) {
					String label = typeParser.toString(value);

					facetValue = new FacetValue();
					facetValue.setValue(value);
					facetValue.setLabel(label);
					facetValue.setFilterSpec(path.filter(FilterOperator.EQ, value));

					facetValueMap.put(value, facetValue);
				}
				facetValue.setCount(facetValue.getCount() + 1);
			}
			return toList(facetValueMap);
		} else {
			throw new UnsupportedOperationException("unknown facet type: " + facetInformation);
		}
	}

	@Override
	public int getPriority() {
		return 1000;
	}
}
