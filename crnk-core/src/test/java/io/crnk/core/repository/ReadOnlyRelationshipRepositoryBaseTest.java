package io.crnk.core.repository;

import org.junit.Test;

public class ReadOnlyRelationshipRepositoryBaseTest {


	private ReadOnlyRelationshipRepositoryBase repo = new ReadOnlyRelationshipRepositoryBase() {

	};

	@Test(expected = UnsupportedOperationException.class)
	public void getSourceResourceClass() {
		repo.getSourceResourceClass();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getMatcher() {
		repo.getMatcher();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getTargetResourceClass() {
		repo.getTargetResourceClass();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findOneTarget() {
		repo.findOneTarget(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findManyTargets() {
		repo.findManyTargets(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setRelation() {
		repo.setRelation(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setRelations() {
		repo.setRelations(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void addRelations() {
		repo.setRelations(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void removeRelations() {
		repo.removeRelations(null, null, null);
	}
}
