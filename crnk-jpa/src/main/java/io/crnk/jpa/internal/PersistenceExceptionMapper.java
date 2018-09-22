package io.crnk.jpa.internal;

import io.crnk.core.module.Module;

import javax.persistence.PersistenceException;

/**
 * PersistenceExceptions can hide the more interesting causes.
 */
public class PersistenceExceptionMapper extends AbstractWrappedExceptionMapper<PersistenceException> {


	public PersistenceExceptionMapper(Module.ModuleContext context) {
		super(context);
	}
}
