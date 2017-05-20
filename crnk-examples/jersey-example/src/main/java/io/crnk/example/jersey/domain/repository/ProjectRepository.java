package io.crnk.example.jersey.domain.repository;

import io.crnk.example.jersey.domain.model.Project;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

public class ProjectRepository implements ResourceRepository<Project, Long> {
	@Override
	public <S extends Project> S save(S entity) {
		return null;
	}

	@Override
	public Project findOne(Long aLong, QueryParams requestParams) {
		return null;
	}

	@Override
	public Iterable<Project> findAll(QueryParams requestParams) {
		return null;
	}

	@Override
	public Iterable<Project> findAll(Iterable<Long> projectIds, QueryParams requestParams) {
		return null;
	}

	@Override
	public void delete(Long aLong) {

	}
}
