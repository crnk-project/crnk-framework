package io.crnk.jpa.util;

import io.crnk.core.engine.transaction.TransactionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;

public class SpringTransactionRunner implements TransactionRunner {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Override
	public <T> T doInTransaction(final Callable<T> callable) {
		TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
		return template.execute(new TransactionCallback<T>() {

			@Override
			public T doInTransaction(TransactionStatus status) {
				try {
					return callable.call();
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
}
