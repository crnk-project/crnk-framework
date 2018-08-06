package io.crnk.activiti.internal.repository;


import java.util.List;

import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.core.queryspec.FilterSpec;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;

public class ProcessInstanceHistoryResourceRepository<T extends ProcessInstanceResource> extends ActivitiRepositoryBase<T> {

	private final HistoryService historyService;

	public ProcessInstanceHistoryResourceRepository(HistoryService historyService, ActivitiResourceMapper resourceMapper,
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
		return resourceMapper.mapToResource(getResourceClass(), (ProcessInstance) result);
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
