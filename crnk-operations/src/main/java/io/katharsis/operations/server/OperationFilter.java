package io.crnk.operations.server;

import io.crnk.operations.OperationResponse;

import java.util.List;


public interface OperationFilter {

	List<OperationResponse> filter(OperationFilterContext context, OperationFilterChain chain);

}
