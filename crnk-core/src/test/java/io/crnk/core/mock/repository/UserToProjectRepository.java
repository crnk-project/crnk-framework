package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.util.Relation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserToProjectRepository implements RelationshipRepository<User, Long, Project, Long> {

    private final ConcurrentMap<Relation<User>, Integer> resources = new ConcurrentHashMap<>();


    @Override
    public Class<User> getSourceResourceClass() {
        return User.class;
    }

    @Override
    public Class<Project> getTargetResourceClass() {
        return Project.class;
    }

    @Override
    public void setRelation(User source, Long targetId, String fieldName) {
        removeRelations(fieldName);
        if (targetId != null) {
            resources.put(new Relation<>(source, targetId, fieldName), 0);
        }
    }

    @Override
    public void setRelations(User source, Collection<Long> targetIds, String fieldName) {
        removeRelations(fieldName);
        if (targetIds != null) {
            for (Long targetId : targetIds) {
                resources.put(new Relation<>(source, targetId, fieldName), 0);
            }
        }
    }

    @Override
    public void addRelations(User source, Collection<Long> targetIds, String fieldName) {
        for (Long targetId : targetIds) {
            resources.put(new Relation<>(source, targetId, fieldName), 0);
        }
    }

    @Override
    public void removeRelations(User source, Collection<Long> targetIds, String fieldName) {
        for (Long targetId : targetIds) {
            Iterator<Relation<User>> iterator = resources.keySet().iterator();
            while (iterator.hasNext()) {
                Relation<User> next = iterator.next();
                if (next.getFieldName().equals(fieldName) && next.getTargetId().equals(targetId)) {
                    iterator.remove();
                }
            }
        }
    }

    public void removeRelations(String fieldName) {
        Iterator<Relation<User>> iterator = resources.keySet().iterator();
        while (iterator.hasNext()) {
            Relation<User> next = iterator.next();
            if (next.getFieldName().equals(fieldName)) {
                iterator.remove();
            }
        }
    }

    @Override
    public Project findOneTarget(Long sourceId, String fieldName, QuerySpec querySpec) {
        for (Relation<User> relation : resources.keySet()) {
            if (relation.getSource().getLoginId().equals(sourceId) &&
                    relation.getFieldName().equals(fieldName)) {
                Project project = new Project();
                project.setId((Long) relation.getTargetId());
                return project;
            }
        }
        return null;
    }

    @Override
    public ResourceList<Project> findManyTargets(Long sourceId, String fieldName, QuerySpec querySpec) {
        ResourceList<Project> projects = new DefaultResourceList<>();
        for (Relation<User> relation : resources.keySet()) {
            if (relation.getSource().getLoginId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
                Project project = new Project();
                project.setId((Long) relation.getTargetId());
                projects.add(project);
            }
        }
        return projects;
    }
}
