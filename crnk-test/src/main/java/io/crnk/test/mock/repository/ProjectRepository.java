package io.crnk.test.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Project.ProjectLinks;
import io.crnk.test.mock.models.Project.ProjectMeta;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectRepository implements ResourceRepository<Project, Long>, MetaRepository<Project>, LinksRepository<Project> {

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
    public <S extends Project> S create(S resource) {
        return save(resource);
    }

    @Override
    public Class<Project> getResourceClass() {
        return Project.class;
    }

    @Override
    public Project findOne(Long aLong, QuerySpec querySpec) {
        Project project = THREAD_LOCAL_REPOSITORY.get(aLong);
        if (project == null) {
            throw new ResourceNotFoundException(Project.class.getCanonicalName());
        }
        return project;
    }

    @Override
    public ResourceList<Project> findAll(QuerySpec querySpec) {
        DefaultResourceList<Project> list = new DefaultResourceList<>();
        list.setMeta(getMetaInformation(list, querySpec));
        list.setLinks(getLinksInformation(list, querySpec));
        querySpec.apply(THREAD_LOCAL_REPOSITORY.values(), list);
        return list;
    }

    @Override
    public ResourceList<Project> findAll(Collection<Long> ids, QuerySpec querySpec) {
        List<Project> values = new LinkedList<>();
        for (Project value : THREAD_LOCAL_REPOSITORY.values()) {
            if (contains(value, ids)) {
                values.add(value);
            }
        }
        return querySpec.apply(values);
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
    public LinksInformation getLinksInformation(Collection<Project> resources, QuerySpec querySpec) {
        ProjectsLinksInformation info = new ProjectsLinksInformation();
        info.setLinkValue("testLink");
        return info;
    }

    @Override
    public MetaInformation getMetaInformation(Collection<Project> resources, QuerySpec querySpec) {
        ProjectsMetaInformation info = new ProjectsMetaInformation();
        info.setMetaValue("testMeta");
        return info;
    }

    public static class ProjectsLinksInformation extends DefaultPagedLinksInformation {

        private String linkValue;

        public String getLinkValue() {
            return linkValue;
        }

        public void setLinkValue(String linkValue) {
            this.linkValue = linkValue;
        }
    }

    public static class ProjectsMetaInformation extends DefaultPagedMetaInformation {

        private String metaValue;

        public String getMetaValue() {
            return metaValue;
        }

        public void setMetaValue(String metaValue) {
            this.metaValue = metaValue;
        }
    }
}
