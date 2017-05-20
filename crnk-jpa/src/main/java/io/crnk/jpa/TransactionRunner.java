package io.crnk.jpa;

import java.util.concurrent.Callable;

@Deprecated
/**
 * @Deprecated use io.crnk.internal.boot.TransactionRunner
 */
public interface TransactionRunner extends io.crnk.core.engine.transaction.TransactionRunner {

	@Override
	<T> T doInTransaction(Callable<T> callable);
}
