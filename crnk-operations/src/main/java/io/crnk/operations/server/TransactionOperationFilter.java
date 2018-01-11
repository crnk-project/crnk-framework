package io.crnk.operations.server;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.operations.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TransactionOperationFilter implements OperationFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionOperationFilter.class);


	@Override
	public List<OperationResponse> filter(final OperationFilterContext context, final OperationFilterChain chain) {
		ServiceDiscovery serviceDiscovery = context.getServiceDiscovery();
		List<TransactionRunner> transactionRunners = serviceDiscovery.getInstancesByType(TransactionRunner.class);
		PreconditionUtil.assertEquals("expected single transaction runner", 1, transactionRunners.size());
		TransactionRunner transactionRunner = transactionRunners.get(0);
		try {
			return transactionRunner.doInTransaction(() -> chain.doFilter(context));
		} catch (Exception e) {
			LOGGER.error("failed to execute operation", e);
			return null;
		}
	}
}
