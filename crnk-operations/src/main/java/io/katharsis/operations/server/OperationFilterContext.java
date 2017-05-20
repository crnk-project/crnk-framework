package io.crnk.operations.server;

import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.operations.server.order.OrderedOperation;

import java.util.List;

public interface OperationFilterContext {

	List<OrderedOperation> getOrderedOperations();

	ServiceDiscovery getServiceDiscovery();
}
