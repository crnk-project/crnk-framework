package io.crnk.core.resource.registry;

import java.util.Arrays;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.UserRepository;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("unchecked")
public class RegistryEntryFacadeTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private UserRepository repository = new UserRepository();

	private RegistryEntry entry;

	private ResourceRepository<User, Long> facade;

	private User user1;

	private User user2;


	@Before
	public void setup() {
		CoreTestContainer container = new CoreTestContainer();
		container.setDefaultPackage();
		container.boot();

		user1 = new User();
		user1.setName("test1");
		user1.setLoginId(13L);
		repository.save(user1);

		user2 = new User();
		user2.setName("test2");
		user2.setLoginId(14L);
		repository.save(user2);

		entry = container.getEntry(User.class);
		facade = entry.getResourceRepositoryFacade();
	}

	@Test
	public void checkFindOne() {
		User facadeUser = facade.findOne(user1.getLoginId(), new QuerySpec(User.class));
		Assert.assertSame(user1, facadeUser);
	}

	@Test
	public void checkSave() {
		User updatedUser = new User();
		updatedUser.setLoginId(user1.getLoginId());
		updatedUser.setName("updated");
		facade.save(updatedUser);

		User facadeUser = facade.findOne(user1.getLoginId(), new QuerySpec(User.class));
		Assert.assertSame(updatedUser, facadeUser);
	}

	@Test
	public void checkDelete() {
		facade.delete(user2.getLoginId());

		ResourceList<User> list = facade.findAll(new QuerySpec(User.class));
		Assert.assertEquals(1, list.size());
	}

	@Test
	public void checkCreate() {
		User updatedUser = new User();
		updatedUser.setName("updated");
		facade.save(updatedUser);

		ResourceList<User> list = facade.findAll(new QuerySpec(User.class));
		Assert.assertEquals(3, list.size());
	}

	@Test
	public void checkFindAllWithIds() {
		ResourceList<User> list = facade.findAll(Arrays.asList(user1.getLoginId()), new QuerySpec(User.class));
		Assert.assertEquals(1, list.size());
		Assert.assertSame(user1, list.get(0));
	}

	@Test
	public void checkFindAll() {
		ResourceList<User> list = facade.findAll(new QuerySpec(User.class));
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void checkFindAllWithFilter() {
		QuerySpec querySpec = new QuerySpec(User.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("loginId"), FilterOperator.EQ, user2.getLoginId()));
		ResourceList<User> list = facade.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		Assert.assertSame(user2, list.get(0));
	}

}
