package io.crnk.core.repository.forward;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.repository.RelationshipRepositoryBase;
import org.junit.Assert;
import org.junit.Test;

@Deprecated
public class RelationshipRepositoryBaseTest {

	@Test
	public void hasDefaultConstructor() {
		CoreClassTestUtils.assertProtectedConstructor(RelationshipRepositoryBase.class);
	}

	@Test
	public void checkAccessors() {
		RelationshipRepositoryBase base = new RelationshipRepositoryBase(Task.class, Project.class);
		Assert.assertEquals(Task.class, base.getSourceResourceClass());
		Assert.assertEquals(Project.class, base.getTargetResourceClass());
	}

	@Test
	public void checkConstructors() {
		new RelationshipRepositoryBase("a", "b");
		new RelationshipRepositoryBase("a");
	}
}
