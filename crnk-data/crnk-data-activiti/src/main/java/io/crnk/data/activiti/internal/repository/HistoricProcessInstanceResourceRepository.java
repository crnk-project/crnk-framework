package io.crnk.data.activiti.internal.repository;


import io.crnk.data.activiti.mapper.ActivitiResourceMapper;
import io.crnk.data.activiti.resource.HistoricProcessInstanceResource;
import io.crnk.core.queryspec.FilterSpec;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;

import java.util.List;

public class HistoricProcessInstanceResourceRepository<T extends HistoricProcessInstanceResource>
		extends ActivitiRepositoryBase<T> {

	private final HistoryService historyService;

	public HistoricProcessInstanceResourceRepository(HistoryService historyService, ActivitiResourceMapper resourceMapper,
													 Class<T> resourceClass, List<FilterSpec> baseFilters) {
		super(resourceMapper, resourceClass, baseFilters);
		this.historyService = historyService;
	}

	@Override
	protected HistoricProcessInstanceQuery createQuery() {
		return historyService.createHistoricProcessInstanceQuery().includeProcessVariables();
	}

	@Override
	protected T mapResult(Object result) {
		return resourceMapper.mapToResource(getResourceClass(), (HistoricProcessInstance) result);
	}

	@Override
	public <S extends T> S create(S resource) {
		throw new UnsupportedOperationException("history cannot be modified");
	}

	@Override
	public <S extends T> S save(S resource) {
		throw new UnsupportedOperationException("history cannot be modified");
	}

	@Override
	public void delete(String id) {
		throw new UnsupportedOperationException("history cannot be modified");
	}
}
