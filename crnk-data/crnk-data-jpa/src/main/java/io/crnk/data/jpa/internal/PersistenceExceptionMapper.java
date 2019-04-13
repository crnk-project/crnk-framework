package io.crnk.data.jpa.internal;

import io.crnk.core.module.Module;

import javax.persistence.PersistenceException;

/**
 * PersistenceExceptions can hide the more interesting causes.
 */
public class PersistenceExceptionMapper extends AbstractJpaExceptionMapper<PersistenceException> {


	public PersistenceExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
