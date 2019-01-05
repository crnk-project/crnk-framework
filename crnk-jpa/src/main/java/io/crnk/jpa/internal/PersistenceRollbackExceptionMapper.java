package io.crnk.jpa.internal;

import io.crnk.core.module.Module;

import javax.persistence.RollbackException;

/**
 * RollbackException can hide the more interesting causes.
 */
public class PersistenceRollbackExceptionMapper extends AbstractJpaExceptionMapper<RollbackException> {


	public PersistenceRollbackExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
