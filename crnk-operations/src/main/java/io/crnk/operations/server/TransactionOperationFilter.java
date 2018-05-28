package io.crnk.operations.server;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.operations.OperationResponse;

import java.util.List;

public class TransactionOperationFilter implements OperationFilter {


	@Override
	public List<OperationResponse> filter(final OperationFilterContext context, final OperationFilterChain chain) {
		ServiceDiscovery serviceDiscovery = context.getServiceDiscovery();
		List<TransactionRunner> transactionRunners = serviceDiscovery.getInstancesByType(TransactionRunner.class);
		PreconditionUtil.verify(1 == transactionRunners.size(), "expected single transaction runner, got %s", transactionRunners);
		TransactionRunner transactionRunner = transactionRunners.get(0);
		return transactionRunner.doInTransaction(() -> chain.doFilter(context));
	}
}
