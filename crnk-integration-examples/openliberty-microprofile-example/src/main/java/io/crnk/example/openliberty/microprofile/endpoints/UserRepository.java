package io.crnk.example.openliberty.microprofile.endpoints;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.openliberty.microprofile.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserRepository extends ResourceRepositoryBase<User, String> {

	private Map<String, User> users = new HashMap<>();

	public UserRepository() {
		super(User.class);
		List<String> interests = new ArrayList<>();
		interests.add("coding");
		interests.add("art");
		save(new User(UUID.randomUUID().toString(), "grogdj@gmail.com", "grogdj", "grogj", "dj", interests));
		save(new User(UUID.randomUUID().toString(), "bot@gmail.com", "bot", "bot", "harry", interests));
		save(new User(UUID.randomUUID().toString(), "evilbot@gmail.com", "evilbot", "bot", "john", interests));
	}

	@Override
	public synchronized void delete(String id) {
		users.remove(id);
	}

	@Override
	public synchronized <S extends User> S save(S user) {
		if (user.getId() == null) {
			user.setId(UUID.randomUUID().toString());
		}
		users.put(user.getId(), user);
		return user;
	}

	@Override
	public synchronized ResourceList<User> findAll(QuerySpec querySpec) {
		return querySpec.apply(users.values());
	}
}
