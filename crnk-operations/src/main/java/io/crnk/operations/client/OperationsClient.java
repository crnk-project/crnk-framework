package io.crnk.operations.client;


import io.crnk.client.CrnkClient;

public class OperationsClient {

	private CrnkClient crnk;

	public OperationsClient(CrnkClient crnk) {
		this.crnk = crnk;
	}

	public OperationsCall createCall() {
		return new OperationsCall(this);
	}

	protected CrnkClient getCrnk() {
		return crnk;
	}
}
