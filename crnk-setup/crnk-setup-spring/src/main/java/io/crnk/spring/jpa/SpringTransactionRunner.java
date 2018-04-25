package io.crnk.spring.jpa;

import java.util.concurrent.Callable;

import io.crnk.core.engine.transaction.TransactionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class SpringTransactionRunner implements TransactionRunner {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Override
	public <T> T doInTransaction(final Callable<T> callable) {
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionTemplate template = new TransactionTemplate(platformTransactionManager, definition);
		try {
			return template.execute(new TransactionCallback<T>() {

				@Override
				public T doInTransaction(TransactionStatus status) {
					try {
						T result = callable.call();
						if (status.isRollbackOnly()) {
							// TransactionTemplate does not properly deal with Rollback exceptions
							// an exception is required, otherwise it will attempt to commit again
							throw new RollbackOnlyException(result);
						}
						return result;
					}
					catch (RuntimeException e) {
						throw e;
					}
					catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			});
		}
		catch (RollbackOnlyException e) {
			return (T) e.getResult();
		}
	}

	class RollbackOnlyException extends RuntimeException {

		private transient Object result;

		public RollbackOnlyException(Object result) {
			this.result = result;
		}

		public Object getResult() {
			return result;
		}
	}
}

