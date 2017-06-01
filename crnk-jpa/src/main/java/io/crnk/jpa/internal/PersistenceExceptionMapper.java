package io.crnk.jpa.internal;

import javax.persistence.PersistenceException;

import io.crnk.core.module.Module;

/**
 * PersistenceExceptions can hide the more interesting causes.
 */
public class PersistenceExceptionMapper extends AbstractWrappedExceptionMapper<PersistenceException> {


	public PersistenceExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
