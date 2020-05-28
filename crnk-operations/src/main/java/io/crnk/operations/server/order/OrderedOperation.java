package io.crnk.operations.server.order;

import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.operations.Operation;

public class OrderedOperation {

	private Operation operation;

	private int ordinal;

	private JsonPath path;

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

	public JsonPath getPath() {
		return path;
	}

	public void setPath(JsonPath path) {
		this.path = path;
	}
}
