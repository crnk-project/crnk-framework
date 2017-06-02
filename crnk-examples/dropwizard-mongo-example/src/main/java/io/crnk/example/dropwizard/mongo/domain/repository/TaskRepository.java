package io.crnk.example.dropwizard.mongo.domain.repository;

import io.crnk.example.dropwizard.mongo.domain.model.Task;
import io.crnk.example.dropwizard.mongo.managed.MongoManaged;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;

import javax.inject.Inject;

public class TaskRepository implements ResourceRepository<Task, ObjectId> {
	private Datastore datastore;

	@Inject
	public TaskRepository(MongoManaged mongoManaged) {
		datastore = mongoManaged.getDatastore();
	}

	public <S extends Task> S save(S entity) {
		Key<Task> saveKey = (Key<Task>) datastore.save(entity);
		return (S) datastore.getByKey(Task.class, saveKey);
	}

	public Task findOne(ObjectId id, QueryParams requestParams) {
		return datastore.getByKey(Task.class, new Key<>(Task.class, id));
	}

	@Override
	public Iterable<Task> findAll(QueryParams requestParams) {
		return datastore.find(Task.class);
	}

	@Override
	public Iterable<Task> findAll(Iterable<ObjectId> iterable, QueryParams requestParams) {
		return datastore.get(Task.class, iterable);
	}

	public void delete(ObjectId id) {
		datastore.delete(datastore.createQuery(Task.class).filter("_id", id));
	}
}
