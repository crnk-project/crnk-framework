package io.crnk.data.jpa.internal;

import io.crnk.core.module.Module;

import javax.transaction.RollbackException;

/**
 * RollbackException can hide the more interesting causes.
 */
public class TransactionRollbackExceptionMapper extends AbstractJpaExceptionMapper<RollbackException> {


	public TransactionRollbackExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
