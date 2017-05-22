package io.crnk.operations.server;

import io.crnk.operations.OperationResponse;

import java.util.List;

public interface OperationFilterChain {

	List<OperationResponse> doFilter(OperationFilterContext context);

}
