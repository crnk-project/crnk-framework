package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepositoryV2;
import io.crnk.core.repository.MetaRepositoryV2;
import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

public class TaskToProjectRelationshipRepository extends RelationshipRepositoryBase<Task, Long, Project, Long>
		implements MetaRepositoryV2<Project>, LinksRepositoryV2<Project> {

	public TaskToProjectRelationshipRepository() {
		super(Task.class, Project.class);
	}

	@Override
	public LinksInformation getLinksInformation(Iterable<Project> resources, QuerySpec querySpec) {
		return new LinksInformation() {

			public String name = "value";
		};
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Project> resources, QuerySpec querySpec) {
		return new MetaInformation() {

			public String name = "value";
		};
	}

}