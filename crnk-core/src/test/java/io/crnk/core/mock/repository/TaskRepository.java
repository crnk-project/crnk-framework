package io.crnk.core.mock.repository;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TaskRepository extends ResourceRepositoryBase<Task, Long>
        implements MetaRepository<Task>, LinksRepository<Task> {

    public static final String BAD_REQUEST_NAME = "badName";

    private Set<Task> tasks = new HashSet<>();

    private long nextId = 0;

    public TaskRepository() {
        super(Task.class);
    }

    @Override
    public ResourceList<Task> findAll(QuerySpec querySpec) {
        TaskList list = new TaskList();
        querySpec.apply(tasks, list);
        list.setMeta(new DefaultPagedMetaInformation());
        list.setLinks(new DefaultPagedLinksInformation());
        return list;
    }


    @Override
    public <S extends Task> S save(S entity) {
        if (BAD_REQUEST_NAME.equals(entity.getName())) {
            throw new BadRequestException("badName");
        }
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        delete(entity.getId()); // replace current one

        // maintain bidirectional mapping, not perfect, should be done in the resources, but serves its purpose her.
        Project project = entity.getProject();
        if (project != null && !project.getTasks().contains(entity)) {
            project.getTasks().add(entity);
        }

        tasks.add(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Task next = iterator.next();
            if (next.getId().equals(id)) {
                iterator.remove();
            }
        }
    }

    @Override
    public LinksInformation getLinksInformation(Collection<Task> resources, QuerySpec queryParams) {
        return new LinksInformation() {

            public String name = "value";
        };
    }

    @Override
    public MetaInformation getMetaInformation(Collection<Task> resources, QuerySpec queryParams) {
        return new MetaInformation() {

            public String name = "value";
        };
    }
}