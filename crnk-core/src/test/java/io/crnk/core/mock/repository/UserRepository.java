package io.crnk.core.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.util.concurrent.ConcurrentHashMap;

public class UserRepository implements ResourceRepositoryV2<User, Long> {

	private static final ConcurrentHashMap<Long, User> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public <S extends User> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		}
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public User findOne(Long aLong, QuerySpec queryParams) {
		User user = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (user == null) {
			throw new ResourceNotFoundException(User.class.getCanonicalName());
		}

		return user;
	}

	@Override
	public ResourceList<User> findAll(QuerySpec queryParams) {
		return queryParams.apply(THREAD_LOCAL_REPOSITORY.values());
	}

	@Override
	public ResourceList<User> findAll(Iterable<Long> ids, QuerySpec queryParams) {
		DefaultResourceList<User> values = new DefaultResourceList<>();
		for (User value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(User value, Iterable<Long> ids) {
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
	public Class<User> getResourceClass() {
		return User.class;
	}

	@Override
	public <S extends User> S create(S entity) {
		return save(entity);
	}
}
