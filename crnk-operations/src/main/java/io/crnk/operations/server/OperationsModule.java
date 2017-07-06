//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.crnk.operations.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.crnk.core.module.Module;
import io.crnk.operations.server.order.DependencyOrderStrategy;
import io.crnk.operations.server.order.OperationOrderStrategy;

public class OperationsModule implements Module {

	private OperationOrderStrategy orderStrategy = new DependencyOrderStrategy();

	private List<io.crnk.operations.server.OperationFilter> filters = new CopyOnWriteArrayList<>();

	public static OperationsModule create(){
		return new OperationsModule();
	}

	// protected for CDI
	protected OperationsModule(){
	}

	public void addFilter(io.crnk.operations.server.OperationFilter filter) {
		this.filters.add(filter);
	}

	public void removeFilter(io.crnk.operations.server.OperationFilter filter) {
		this.filters.remove(filter);
	}

	public OperationOrderStrategy getOrderStrategy() {
		return orderStrategy;
	}

	public void setOrderStrategy(OperationOrderStrategy orderStrategy) {
		this.orderStrategy = orderStrategy;
	}

	public List<io.crnk.operations.server.OperationFilter> getFilters() {
		return filters;
	}

	@Override
	public String getModuleName() {
		return "operations";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addHttpRequestProcessor(new io.crnk.operations.server.OperationsRequestProcessor(this, context));
	}
}
