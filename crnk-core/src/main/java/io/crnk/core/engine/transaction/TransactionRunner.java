package io.crnk.core.engine.transaction;

import java.util.concurrent.Callable;

public interface TransactionRunner {

	<T> T doInTransaction(Callable<T> callable) throws Exception;
}
