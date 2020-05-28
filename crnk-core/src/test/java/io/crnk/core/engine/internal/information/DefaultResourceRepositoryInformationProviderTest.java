package io.crnk.core.engine.internal.information;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;
import org.junit.Test;

public class DefaultResourceRepositoryInformationProviderTest {

	@Test
	public void checkReadonlyResourceRepository() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
		Object repository = new ReadOnlyResourceRepositoryBase(Task.class) {
			@Override
			public ResourceList findAll(QuerySpec querySpec) {
				return null;
			}
		};
		RepositoryInformation information = boot.getModuleRegistry().getRepositoryInformation(repository);
		RepositoryMethodAccess actualAccess = information.getAccess();
		Assert.assertFalse(actualAccess.isPatchable());
		Assert.assertFalse(actualAccess.isPostable());
		Assert.assertFalse(actualAccess.isDeletable());
		Assert.assertTrue(actualAccess.isReadable());
	}


	@Test
	public void checkCustomDeletableResourceRepository() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
		Object repository = new ResourceRepositoryBase(Task.class) {
			@Override
			public ResourceList findAll(QuerySpec querySpec) {
				return null;
			}

			@Override
			public void delete(Object id) {

			}
		};
		RepositoryInformation information = boot.getModuleRegistry().getRepositoryInformation(repository);
		RepositoryMethodAccess actualAccess = information.getAccess();
		Assert.assertFalse(actualAccess.isPatchable());
		Assert.assertFalse(actualAccess.isPostable());
		Assert.assertTrue(actualAccess.isDeletable());
		Assert.assertTrue(actualAccess.isReadable());
	}

	@Test
	public void checkCustomPatchableResourceRepository() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
		Object repository = new ResourceRepositoryBase(Task.class) {
			@Override
			public ResourceList findAll(QuerySpec querySpec) {
				return null;
			}

			@Override
			public Object save(Object task) {
				return task;
			}
		};
		RepositoryInformation information = boot.getModuleRegistry().getRepositoryInformation(repository);
		RepositoryMethodAccess actualAccess = information.getAccess();
		Assert.assertTrue(actualAccess.isPatchable());
		Assert.assertFalse(actualAccess.isPostable());
		Assert.assertFalse(actualAccess.isDeletable());
		Assert.assertTrue(actualAccess.isReadable());
	}

	@Test
	public void checkCustomPostableResourceRepository() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
		Object repository = new ResourceRepositoryBase(Task.class) {
			@Override
			public ResourceList findAll(QuerySpec querySpec) {
				return null;
			}

			@Override
			public Object create(Object task) {
				return task;
			}
		};
		RepositoryInformation information = boot.getModuleRegistry().getRepositoryInformation(repository);
		RepositoryMethodAccess actualAccess = information.getAccess();
		Assert.assertFalse(actualAccess.isPatchable());
		Assert.assertTrue(actualAccess.isPostable());
		Assert.assertFalse(actualAccess.isDeletable());
		Assert.assertTrue(actualAccess.isReadable());
	}

}
