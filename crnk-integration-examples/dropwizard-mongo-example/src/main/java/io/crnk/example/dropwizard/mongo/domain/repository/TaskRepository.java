package io.crnk.example.dropwizard.mongo.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.dropwizard.mongo.domain.model.Task;
import io.crnk.example.dropwizard.mongo.managed.MongoManaged;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;

import javax.inject.Inject;

public class TaskRepository implements ResourceRepositoryV2<Task, ObjectId> {

	private Datastore datastore;

	@Inject
	public TaskRepository(MongoManaged mongoManaged) {
		datastore = mongoManaged.getDatastore();
	}

	public <S extends Task> S save(S entity) {
		Key<Task> saveKey = (Key<Task>) datastore.save(entity);
		return (S) datastore.getByKey(Task.class, saveKey);
	}

	@Override
	public <S extends Task> S create(S entity) {
		return save(entity);
	}

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	public Task findOne(ObjectId id, QuerySpec requestParams) {
		return datastore.getByKey(Task.class, new Key<>(Task.class, id));
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec requestParams) {
		DefaultResourceList<Task> results = new DefaultResourceList<>();
		results.addAll(datastore.find(Task.class).asList());
		return results;
	}

	@Override
	public ResourceList<Task> findAll(Iterable<ObjectId> iterable, QuerySpec requestParams) {
		DefaultResourceList<Task> results = new DefaultResourceList<>();
		results.addAll(datastore.get(Task.class, iterable).asList());
		return results;

	}

	public void delete(ObjectId id) {
		datastore.delete(datastore.createQuery(Task.class).filter("_id", id));
	}
}
