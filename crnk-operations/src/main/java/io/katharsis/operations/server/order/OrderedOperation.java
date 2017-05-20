package io.crnk.operations.server.order;

import io.crnk.operations.Operation;

public class OrderedOperation {

	private Operation operation;

	private int ordinal;

	public OrderedOperation(Operation operation, int ordinal) {
		this.operation = operation;
		this.ordinal = ordinal;
	}

	public Operation getOperation() {
		return operation;
	}

	public int getOrdinal() {
		return ordinal;
	}
}
