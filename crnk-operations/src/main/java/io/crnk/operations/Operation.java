package io.crnk.operations;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.internal.utils.CompareUtils;

public class Operation {

	private String op;

	private String path;

	private Resource value;

	public Operation() {
	}

	public Operation(String op, String path, Resource value) {
		this.op = op;
		this.path = path;
		this.value = value;
	}

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
		if (o != null && getClass() == o.getClass()) {
			Operation operation = (Operation) o;
			return CompareUtils.isEquals(op, operation.op) && CompareUtils.isEquals(path, operation.path) && CompareUtils.isEquals(value, operation.value);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = op != null ? op.hashCode() : 0;
		result = 31 * result + (path != null ? path.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}
}
