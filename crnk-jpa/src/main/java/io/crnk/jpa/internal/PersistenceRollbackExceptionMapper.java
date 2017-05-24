package io.crnk.jpa.internal;

import javax.persistence.RollbackException;

import io.crnk.core.module.Module;

/**
 * RollbackException can hide the more interesting causes.
 */
public class PersistenceRollbackExceptionMapper extends AbstractWrappedExceptionMapper<RollbackException> {


	public PersistenceRollbackExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
