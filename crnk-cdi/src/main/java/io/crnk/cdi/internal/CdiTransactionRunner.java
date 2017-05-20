package io.crnk.cdi.internal;

import io.crnk.core.engine.transaction.TransactionRunner;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionalException;
import java.util.concurrent.Callable;

/**
 * Runs within CDI/JEE container.
 */
@ApplicationScoped
public class CdiTransactionRunner implements TransactionRunner {

	private CdiTransactionRunnerImpl impl;

	// A no args constructor is required. See #280 for details.
	public CdiTransactionRunner() {
	}

	@Inject
	public CdiTransactionRunner(CdiTransactionRunnerImpl impl) {
		this.impl = impl;
	}

	@Override
	public <T> T doInTransaction(Callable<T> callable) {
		try {
			return impl.doInTransaction(callable);
		} catch (TransactionalException e) {
			// unwrap since not usable, cause more interesting
			// (validationException, etc.)
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw e;
		}
	}
}
