package io.crnk.jpa.internal;

import javax.transaction.RollbackException;

import io.crnk.core.module.Module;

/**
 * RollbackException can hide the more interesting causes.
 */
public class TransactionRollbackExceptionMapper extends AbstractWrappedExceptionMapper<RollbackException> {


	public TransactionRollbackExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
