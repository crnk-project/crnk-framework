package io.crnk.operations.server;

import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.operations.OperationResponse;

import java.util.List;
import java.util.concurrent.Callable;

public class TransactionOperationFilter implements OperationFilter {


	@Override
	public List<OperationResponse> filter(final OperationFilterContext context, final OperationFilterChain chain) {
		ServiceDiscovery serviceDiscovery = context.getServiceDiscovery();
		List<TransactionRunner> transactionRunners = serviceDiscovery.getInstancesByType(TransactionRunner.class);
		if (transactionRunners.size() != 1) {
			throw new IllegalStateException("expected single transaction runner, got " + transactionRunners);
		}
		TransactionRunner transactionRunner = transactionRunners.get(0);
		return transactionRunner.doInTransaction(new Callable<List<OperationResponse>>() {
			@Override
			public List<OperationResponse> call() throws Exception {
				return chain.doFilter(context);
			}
		});
	}
}
