package io.crnk.operations;

import io.crnk.core.engine.document.Resource;

public class Operation {

	private String op;

	private String path;

	private Resource value;

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Resource getValue() {
		return value;
	}

	public void setValue(Resource value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Operation operation = (Operation) o;

		if (op != null ? !op.equals(operation.op) : operation.op != null) {
			return false;
		}
		if (path != null ? !path.equals(operation.path) : operation.path != null) {
			return false;
		}
		return value != null ? value.equals(operation.value) : operation.value == null;
	}

	@Override
	public int hashCode() {
		int result = op != null ? op.hashCode() : 0;
		result = 31 * result + (path != null ? path.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}
}
