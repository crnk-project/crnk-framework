package io.crnk.jpa.internal;

import io.crnk.core.module.Module;

import javax.transaction.RollbackException;

/**
 * RollbackException can hide the more interesting causes.
 */
public class TransactionRollbackExceptionMapper extends AbstractWrappedExceptionMapper<RollbackException> {


	public TransactionRollbackExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
