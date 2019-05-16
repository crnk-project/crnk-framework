package io.crnk.test.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyLinksRepository;
import io.crnk.legacy.repository.LegacyMetaRepository;
import io.crnk.legacy.repository.LegacyResourceRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Project.ProjectLinks;
import io.crnk.test.mock.models.Project.ProjectMeta;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectRepository implements LegacyResourceRepository<Project, Long>, LegacyMetaRepository<Project>, LegacyLinksRepository<Project> {

	private static final ConcurrentHashMap<Long, Project> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public <S extends Project> S save(S entity) {
		entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		if (entity.getLinks() == null) {
			entity.setLinks(new ProjectLinks());
		}
		if (entity.getLinks().getValue() == null) {
			entity.getLinks().setValue("someLinkValue");
		}

		if (entity.getMeta() == null) {
			entity.setMeta(new ProjectMeta());
		}
		if (entity.getMeta().getValue() == null) {
			entity.getMeta().setValue("someMetaValue");
		}

		return entity;
	}

	@Override
	public Project findOne(Long aLong, QueryParams queryParams) {
		Project project = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (project == null) {
			throw new ResourceNotFoundException(Project.class.getCanonicalName());
		}
		return project;
	}

	@Override
	public Iterable<Project> findAll(QueryParams queryParamss) {
		return THREAD_LOCAL_REPOSITORY.values();
	}

	@Override
	public Iterable<Project> findAll(Iterable<Long> ids, QueryParams queryParams) {
		List<Project> values = new LinkedList<>();
		for (Project value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Project value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		THREAD_LOCAL_REPOSITORY.remove(aLong);
	}

	@Override
	public LinksInformation getLinksInformation(Iterable<Project> resources, QueryParams queryParams) {
		ProjectsLinksInformation info = new ProjectsLinksInformation();
		info.setLinkValue("testLink");
		return info;
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Project> resources, QueryParams queryParams) {
		ProjectsMetaInformation info = new ProjectsMetaInformation();
		info.setMetaValue("testMeta");
		return info;
	}

	public static class ProjectsLinksInformation implements LinksInformation {

		private String linkValue;

		public String getLinkValue() {
			return linkValue;
		}

		public void setLinkValue(String linkValue) {
			this.linkValue = linkValue;
		}
	}

	public static class ProjectsMetaInformation implements MetaInformation {

		private String metaValue;

		public String getMetaValue() {
			return metaValue;
		}

		public void setMetaValue(String metaValue) {
			this.metaValue = metaValue;
		}
	}
}
