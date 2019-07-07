package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskToProjectRepository implements OneRelationshipRepository<Task, Long, Project, Long>, ManyRelationshipRepository<Task, Long, Project, Long> {

    private static final ConcurrentMap<Relation<Task>, Integer> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

    public static void clear() {
        THREAD_LOCAL_REPOSITORY.clear();
    }

    public TaskToProjectRepository() {

    }

    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().source(Task.class, true).target(Project.class).add();
        return matcher;
    }

    @Override
    public void setRelation(Task source, Long targetId, String fieldName) {
        removeRelations(fieldName);
        if (targetId != null) {
            THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
        }
    }


    @Override
    public void setRelations(Task source, Collection<Long> targetIds, String fieldName) {
        removeRelations(fieldName);
        if (targetIds != null) {
            for (Long targetId : targetIds) {
                THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
            }
        }
    }

    @Override
    public void addRelations(Task source, Collection<Long> targetIds, String fieldName) {
        for (Long targetId : targetIds) {
            THREAD_LOCAL_REPOSITORY.put(new Relation<>(source, targetId, fieldName), 0);
        }
    }

    @Override
    public void removeRelations(Task source, Collection<Long> targetIds, String fieldName) {
        for (Long targetId : targetIds) {
            Iterator<Relation<Task>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
            while (iterator.hasNext()) {
                Relation<Task> next = iterator.next();
                if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public Map<Long, Project> findOneRelations(Collection<Long> sourceIds, String fieldName, QuerySpec querySpec) {
        Map<Long, Project> map = new HashMap<>();
        for (Long sourceId : sourceIds) {
            map.put(sourceId, null);
            for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
                if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
                    Project project = new Project();
                    project.setId((Long) relation.getTargetId());
                    map.put(sourceId, project);
                    break;
                }
            }
        }
        return map;
    }

    @Override
    public Map<Long, ResourceList<Project>> findManyRelations(Collection<Long> sourceIds, String fieldName, QuerySpec querySpec) {
        Map<Long, ResourceList<Project>> map = new HashMap<>();
        for (Long sourceId : sourceIds) {
            DefaultResourceList<Project> projects = new DefaultResourceList<>();
            for (Relation<Task> relation : THREAD_LOCAL_REPOSITORY.keySet()) {
                if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
                    Project project = new Project();
                    project.setId((Long) relation.getTargetId());
                    projects.add(project);
                }
            }
            ProjectRepository.ProjectsLinksInformation links = new ProjectRepository.ProjectsLinksInformation();
            links.setLinkValue("linkValue");
            projects.setLinks(links);

            ProjectRepository.ProjectsMetaInformation meta = new ProjectRepository.ProjectsMetaInformation();
            meta.setMetaValue("metaValue");
            projects.setMeta(meta);

            map.put(sourceId, projects);
        }
        return map;
    }

    public void removeRelations(String fieldName) {
        Iterator<Relation<Task>> iterator = THREAD_LOCAL_REPOSITORY.keySet().iterator();
        while (iterator.hasNext()) {
            Relation<Task> next = iterator.next();
            if (next.getFieldName().equals(fieldName)) {
                iterator.remove();
            }
        }
    }
}
