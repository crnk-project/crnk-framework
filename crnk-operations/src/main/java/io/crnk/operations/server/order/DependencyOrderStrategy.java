package io.crnk.operations.server.order;

import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.operations.Operation;
import io.crnk.operations.internal.GraphUtils;

import java.util.*;

public class DependencyOrderStrategy implements OperationOrderStrategy {

	private static final String toKey(ResourceIdentifier resource) {
		return resource.getType() + "/" + resource.getId();
	}

	@Override
	public List<OrderedOperation> order(List<Operation> operations) {
		Map<String, GraphUtils.Node> nodeMap = new HashMap<>();
		List<GraphUtils.Node> nodeList = new ArrayList<>();
		for (int i = 0; i < operations.size(); i++) {
			Operation operation = operations.get(i);
			String key = toKey(operation.getValue());
			if (nodeMap.containsKey(key)) {
				throw new UnsupportedOperationException("cannot modify same resource with multiple operations: type=" +
						operation.getValue().getType() + " id=" + operation.getValue().getId());
			}

			GraphUtils.Node node = new GraphUtils.Node(key, new OrderedOperation(operation, i));
			nodeMap.put(key, node);
			nodeList.add(node);
		}

		buildDependencyGraph(operations, nodeMap);

		List<GraphUtils.Node> sortedNodes = GraphUtils.sort(nodeList);
		List<OrderedOperation> dependencySortedOperations = new ArrayList<>();
		for (GraphUtils.Node node : sortedNodes) {
			dependencySortedOperations.add((OrderedOperation) node.getValue());
		}
		return moveDeletionsToEnd(dependencySortedOperations);
	}

	private void buildDependencyGraph(List<Operation> operations, Map<String, GraphUtils.Node> nodes) {
		for (Operation operation : operations) {
			Resource resource = operation.getValue();
			for (Relationship relationship : resource.getRelationships().values()) {
				if (!relationship.getData().isPresent()) {
					continue;
				}
				Object data = relationship.getData().get();
				if (data instanceof Collection) {
					for (ResourceIdentifier dependencyId : (Collection<ResourceIdentifier>) data) {
						checkDependency(nodes, operation, dependencyId);
					}
				} else if (data != null) {
					ResourceIdentifier dependencyId = (ResourceIdentifier) data;
					checkDependency(nodes, operation, dependencyId);
				}
			}
		}

	}

	private List<OrderedOperation> moveDeletionsToEnd(List<OrderedOperation> list) {
		List<OrderedOperation> sortedNonDeleteOperations = new ArrayList<>();
		List<OrderedOperation> sortedDeleteOperations = new ArrayList<>();

		for (OrderedOperation operation : list) {
			if (HttpMethod.DELETE.toString().equalsIgnoreCase(operation.getOperation().getOp())) {
				sortedDeleteOperations.add(operation);
			} else {
				sortedNonDeleteOperations.add(operation);
			}
		}

		List<OrderedOperation> sortedOperations = new ArrayList<>();
		sortedOperations.addAll(sortedNonDeleteOperations);
		sortedOperations.addAll(sortedDeleteOperations);
		return sortedOperations;
	}

	private void checkDependency(Map<String, GraphUtils.Node> nodes, Operation operation, ResourceIdentifier
			dependencyId) {
		String dependencyKey = toKey(dependencyId);
		GraphUtils.Node node = nodes.get(toKey(operation.getValue()));
		if (nodes.containsKey(dependencyKey)) {
			GraphUtils.Node dependentNode = nodes.get(dependencyKey);
			Operation dependentOperation = ((OrderedOperation) dependentNode.getValue()).getOperation();
			if (HttpMethod.POST.name().equalsIgnoreCase(dependentOperation.getOp())) {
				dependentNode.addEdge(node);
			}
		}
	}
}
