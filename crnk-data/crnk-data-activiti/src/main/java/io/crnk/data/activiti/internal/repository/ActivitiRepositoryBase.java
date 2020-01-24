package io.crnk.data.activiti.internal.repository;


import io.crnk.data.activiti.mapper.ActivitiQuerySpecMapper;
import io.crnk.data.activiti.mapper.ActivitiResourceMapper;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.activiti.engine.query.Query;

import java.util.Arrays;
import java.util.List;

public abstract class ActivitiRepositoryBase<T> extends ResourceRepositoryBase<T, String> {

	protected final ActivitiResourceMapper resourceMapper;

	protected final List<FilterSpec> baseFilters;


	public ActivitiRepositoryBase(ActivitiResourceMapper resourceMapper, Class<T> resourceClass,
								  List<FilterSpec> baseFilters) {
		super(resourceClass);
		this.baseFilters = baseFilters;
		this.resourceMapper = resourceMapper;
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		Query activitiQuery = createQuery();
		List list = ActivitiQuerySpecMapper.find(activitiQuery, querySpec, baseFilters);
		return mapToResources(list);
	}

	protected void checkFilter(Object resource, boolean applyAsDefault) {


		QuerySpec enforcementSpec = new QuerySpec(resource.getClass());
		for (FilterSpec baseFilter : baseFilters) {
			enforcementSpec.addFilter(baseFilter);

			// apply as default if possible
			List<String> attributePath = baseFilter.getAttributePath();
			if (attributePath.size() == 1 && baseFilter.getOperator() == FilterOperator.EQ) {
				String attributeName = baseFilter.getAttributePath().get(0);
				Object actualValue = PropertyUtils.getProperty(resource, attributeName);
				Object expectedValue = baseFilter.getValue();
				if (actualValue == null) {
					PropertyUtils.setProperty(resource, attributeName, expectedValue);
				}
			}
		}
		if (enforcementSpec.apply(Arrays.asList(resource)).isEmpty()) {
			throw new BadRequestException("resource does not belong to this repository");
		}
	}

	protected abstract Query createQuery();

	private ResourceList<T> mapToResources(List results) {
		DefaultResourceList<T> resources = new DefaultResourceList<>();
		for (Object result : results) {
			resources.add(mapResult(result));

		}
		return resources;
	}

	protected abstract T mapResult(Object result);
}
