package io.crnk.test.mock.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.RelatedLinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
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
        list.setMeta(getMetaInformation(list, querySpec, null));
        list.setLinks(getLinksInformation(list, querySpec, null));
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
    public LinksInformation getLinksInformation(Collection<Project> resources, QuerySpec querySpec, LinksInformation current) {
        ProjectsLinksInformation info = new ProjectsLinksInformation();
        info.setLinkValue("testLink");
        return info;
    }

    @Override
    public MetaInformation getMetaInformation(Collection<Project> resources, QuerySpec querySpec, MetaInformation current) {
        ProjectsMetaInformation info = new ProjectsMetaInformation();
        info.setMetaValue("testMeta");
        return info;
    }

    public static class ProjectsLinksInformation extends DefaultPagedLinksInformation implements SelfLinksInformation, RelatedLinksInformation {

        private String linkValue;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String self;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String related;

        public String getLinkValue() {
            return linkValue;
        }

        public void setLinkValue(String linkValue) {
            this.linkValue = linkValue;
        }

        @Override
        public String getSelf() {
            return self;
        }

        @Override
        public void setSelf(String self) {
            this.self = self;
        }

        @Override
        public String getRelated() {
            return related;
        }

        @Override
        public void setRelated(String related) {
            this.related = related;
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
