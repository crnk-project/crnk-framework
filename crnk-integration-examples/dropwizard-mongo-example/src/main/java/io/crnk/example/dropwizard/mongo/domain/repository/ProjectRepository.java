package io.crnk.example.dropwizard.mongo.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.dropwizard.mongo.domain.model.Project;
import io.crnk.example.dropwizard.mongo.managed.MongoManaged;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;

import javax.inject.Inject;

public class ProjectRepository implements ResourceRepositoryV2<Project, ObjectId> {

	private Datastore datastore;

	@Inject
	public ProjectRepository(MongoManaged mongoManaged) {
		datastore = mongoManaged.getDatastore();
	}

	public <S extends Project> S save(S entity) {
		Key<Project> saveKey = datastore.save(entity);
		return (S) datastore.getByKey(Project.class, saveKey);
	}

	@Override
	public <S extends Project> S create(S entity) {
		return save(entity);
	}

	@Override
	public Class<Project> getResourceClass() {
		return Project.class;
	}

	public Project findOne(ObjectId id, QuerySpec requestParams) {
		return datastore.getByKey(Project.class, new Key<>(Project.class, id));
	}

	@Override
	public ResourceList<Project> findAll(QuerySpec requestParams) {
		DefaultResourceList<Project> results = new DefaultResourceList<>();
		results.addAll(datastore.find(Project.class).asList());
		return results;
	}

	@Override
	public ResourceList<Project> findAll(Iterable<ObjectId> iterable, QuerySpec requestParams) {
		DefaultResourceList<Project> results = new DefaultResourceList<>();
		results.addAll(datastore.get(Project.class, iterable).asList());
		return results;
	}

	public void delete(ObjectId id) {
		datastore.delete(datastore.createQuery(Project.class).filter("_id", id));
	}
}