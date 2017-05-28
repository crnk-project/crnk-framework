package io.crnk.operations;

import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.utils.Nullable;
import io.crnk.operations.server.order.DependencyOrderStrategy;
import io.crnk.operations.server.order.OrderedOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DependencyOrderStrategyTest {

	private DependencyOrderStrategy strategy = new DependencyOrderStrategy();

	@Test
	public void testEmpty() {
		List<Operation> result = toOperations(strategy.order((List) Collections.emptyList()));
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testSingleResource() {
		Operation operation = createOperation("movie", "test", HttpMethod.POST);
		List<Operation> results = toOperations(strategy.order(Arrays.asList(operation)));
		Assert.assertEquals(1, results.size());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCannotOrderDuplicateObjects() {
		Operation operation = createOperation("movie", "test", HttpMethod.POST);
		toOperations(strategy.order(Arrays.asList(operation, operation)));
	}

	@Test
	public void testTwoIndependentResource() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.POST);
		Operation op2 = createOperation("movie", "test2", HttpMethod.POST);
		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op1, results.get(0));
		Assert.assertEquals(op2, results.get(1));
	}

	@Test
	public void testDeleteAfterPost1() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.POST);
		Operation op2 = createOperation("movie", "test2", HttpMethod.DELETE);
		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op1, results.get(0));
		Assert.assertEquals(op2, results.get(1));
	}

	@Test
	public void testDeleteAfterPost2() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.DELETE);
		Operation op2 = createOperation("movie", "test2", HttpMethod.POST);
		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op2, results.get(0));
		Assert.assertEquals(op1, results.get(1));
	}

	@Test
	public void testFirstPostDependsOneOnSecondPost() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.POST);
		Operation op2 = createOperation("person", "test2", HttpMethod.POST);
		addOneDependency(op1, op2, "directors");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op2, results.get(0));
		Assert.assertEquals(op1, results.get(1));
	}

	@Test
	public void testFirstPostDependsManyOnSecondPost() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.POST);
		Operation op2 = createOperation("person", "test2", HttpMethod.POST);
		addManyDependency(op1, op2, "directors");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op2, results.get(0));
		Assert.assertEquals(op1, results.get(1));
	}


	@Test(expected = IllegalStateException.class)
	public void testCyclicPost() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.POST);
		Operation op2 = createOperation("person", "test2", HttpMethod.POST);
		addManyDependency(op1, op2, "directors");
		addManyDependency(op2, op1, "directors");

		strategy.order(Arrays.asList(op1, op2));
	}

	@Test
	public void testSecondPostDependsOnFirstPost() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.POST);
		Operation op2 = createOperation("person", "test2", HttpMethod.POST);
		addManyDependency(op2, op1, "writers");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op1, results.get(0));
		Assert.assertEquals(op2, results.get(1));
	}

	@Test
	public void testSecondPatchDependsOnFirstPatch() {
		Operation op1 = createOperation("movie", "test1", HttpMethod.PATCH);
		Operation op2 = createOperation("person", "test2", HttpMethod.PATCH);
		addManyDependency(op2, op1, "writers");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op1, results.get(0));
		Assert.assertEquals(op2, results.get(1));
	}

	@Test
	public void testFirstPatchDependsOnSecondPatch() {
		Operation op1 = createOperation("movie", "e", HttpMethod.PATCH);
		Operation op2 = createOperation("person", "f", HttpMethod.PATCH);
		addManyDependency(op1, op2, "writers");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op1, results.get(0));
		Assert.assertEquals(op2, results.get(1));
	}


	@Test
	public void testFirstUninitalizedDependencyIsIngored() {
		Operation op1 = createOperation("movie", "c", HttpMethod.POST);
		Operation op2 = createOperation("person", "d", HttpMethod.POST);
		addUnitializedDependency(op1, "writers");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op2, results.get(1));
		Assert.assertEquals(op1, results.get(0));
	}

	@Test
	public void testSecondUninitalizedDependencyIsIngored() {
		Operation op1 = createOperation("movie", "a", HttpMethod.POST);
		Operation op2 = createOperation("person", "b", HttpMethod.POST);
		addUnitializedDependency(op2, "writers");

		List<Operation> results = toOperations(strategy.order(Arrays.asList(op1, op2)));
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(op2, results.get(1));
		Assert.assertEquals(op1, results.get(0));
	}

	private List<Operation> toOperations(List<OrderedOperation> orderedOperations) {
		List<Operation> operations = new ArrayList<>();
		for (OrderedOperation orderedOperation : orderedOperations) {
			operations.add(orderedOperation.getOperation());
		}
		return operations;
	}

	private Operation createOperation(String type, String id, HttpMethod method) {
		Resource resource = new Resource();
		resource.setId(id);
		resource.setType(type);

		Operation operation = new Operation();
		operation.setOp(method.toString());
		if (method == HttpMethod.POST) {
			operation.setPath(type);
		} else {
			operation.setPath(type + "/" + id);
		}
		operation.setValue(resource);
		return operation;
	}

	private void addManyDependency(Operation op1, Operation op2, String relationshipName) {
		Resource resource1 = op1.getValue();
		Resource resource2 = op2.getValue();

		Relationship relationship = resource1.getRelationships().get(relationshipName);
		if (relationship == null) {
			relationship = new Relationship();
			relationship.setData((Nullable) Nullable.of(new ArrayList<ResourceIdentifier>()));
			resource1.getRelationships().put(relationshipName, relationship);
		}

		ResourceIdentifier resourceId = new ResourceIdentifier(resource2.getId(), resource2.getType());
		relationship.getCollectionData().get().add(resourceId);
	}

	private void addOneDependency(Operation op1, Operation op2, String relationshipName) {
		Resource resource1 = op1.getValue();
		Resource resource2 = op2.getValue();

		Relationship relationship = resource1.getRelationships().get(relationshipName);
		if (relationship == null) {
			relationship = new Relationship();
			resource1.getRelationships().put(relationshipName, relationship);
		}

		ResourceIdentifier resourceId = new ResourceIdentifier(resource2.getId(), resource2.getType());
		relationship.setData(Nullable.of((Object) resourceId));
	}

	private void addUnitializedDependency(Operation op, String relationshipName) {
		Resource resource1 = op.getValue();
		Relationship relationship = resource1.getRelationships().get(relationshipName);
		if (relationship == null) {
			relationship = new Relationship();
			resource1.getRelationships().put(relationshipName, relationship);
		}
		relationship.setData(Nullable.empty());
	}
}
