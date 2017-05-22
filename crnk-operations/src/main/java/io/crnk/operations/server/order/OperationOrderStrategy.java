package io.crnk.operations.server.order;

import io.crnk.operations.Operation;

import java.util.List;

public interface OperationOrderStrategy {

	List<OrderedOperation> order(List<Operation> operations);
}
