package io.crnk.core.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository implements ResourceRepository<User, Long> {

    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();


    @Override
    public <S extends User> S save(S entity) {
        if (entity.getLoginId() == null) {
            entity.setLoginId((long) (users.size() + 1));
        }
        users.put(entity.getLoginId(), entity);

        return entity;
    }

    @Override
    public User findOne(Long aLong, QuerySpec queryParams) {
        User user = users.get(aLong);
        if (user == null) {
            throw new ResourceNotFoundException(User.class.getCanonicalName());
        }

        return user;
    }

    @Override
    public ResourceList<User> findAll(QuerySpec queryParams) {
        return queryParams.apply(users.values());
    }

    @Override
    public ResourceList<User> findAll(Collection<Long> ids, QuerySpec queryParams) {
        DefaultResourceList<User> values = new DefaultResourceList<>();
        for (User value : users.values()) {
            if (contains(value, ids)) {
                values.add(value);
            }
        }
        return values;
    }

    private boolean contains(User value, Collection<Long> ids) {
        for (Long id : ids) {
            if (value.getLoginId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void delete(Long aLong) {
        users.remove(aLong);
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
