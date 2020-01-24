package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectRepository extends ResourceRepositoryBase<Project, Long> {

    private final ConcurrentHashMap<Long, Project> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

    public static final long RETURN_NULL_ON_CREATE_ID = 13412423;

	public static final long RETURN_NO_ID_ON_CREATE = 13412424;

    /**
     * That particular ID will be mapped to a fancy project to simulated inheritance
     */
    public static final long FANCY_PROJECT_ID = 101001;

    public ProjectRepository() {
        super(Project.class);
    }

    @Override
    public <S extends Project> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
        }
        THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

        if (entity.getId() == RETURN_NULL_ON_CREATE_ID) {
            return null;
        }
		if (entity.getId() == RETURN_NO_ID_ON_CREATE) {
			Project project = new Project();
			project.setId(null);
			return (S) project;
		}

        return entity;
    }

    @Override
    public ResourceList<Project> findAll(QuerySpec querySpec) {
        return querySpec.apply(THREAD_LOCAL_REPOSITORY.values());
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.remove(aLong);
    }
}
