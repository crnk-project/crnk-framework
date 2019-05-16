package io.crnk.data.jpa;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.data.jpa.internal.HibernateConstraintViolationExceptionMapper;
import io.crnk.data.jpa.internal.PersistenceExceptionMapper;
import io.crnk.data.jpa.internal.PersistenceRollbackExceptionMapper;
import io.crnk.data.jpa.internal.TransactionRollbackExceptionMapper;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.PersistenceException;

public class JpaExceptionMapperTests {

	private CrnkBoot boot;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(JpaModule.newClientModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.data.jpa.mock.model", new SampleJsonServiceLocator()));
		boot.boot();
	}

	@Test
	public void testPersistenceException() {
		PersistenceException exception = new PersistenceException(new BadRequestException("test"));
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		PersistenceExceptionMapper mapper = (PersistenceExceptionMapper) exceptionMapperRegistry.findMapperFor(PersistenceException.class).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}

	@Test
	public void testConstraintException() {
		ConstraintViolationException exception = new ConstraintViolationException("message", null, "constraint");
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		HibernateConstraintViolationExceptionMapper mapper = (HibernateConstraintViolationExceptionMapper) exceptionMapperRegistry.findMapperFor(ConstraintViolationException.class).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.UNPROCESSABLE_ENTITY_422), errorData.getStatus());
		Assert.assertEquals(exception.getConstraintName(), errorData.getCode());
		Assert.assertEquals(exception.getMessage(), errorData.getDetail());

		Assert.assertTrue(mapper.accepts(response));
		ConstraintViolationException deserializedException = mapper.fromErrorResponse(response);
		Assert.assertEquals(exception.getMessage(), deserializedException.getMessage());
		Assert.assertEquals(exception.getConstraintName(), deserializedException.getConstraintName());
	}

	@Test
	public void testPersistenceRollbackException() {
		javax.persistence.RollbackException exception = new javax.persistence.RollbackException(new BadRequestException("test"));
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		PersistenceRollbackExceptionMapper mapper = (PersistenceRollbackExceptionMapper) exceptionMapperRegistry.findMapperFor(javax.persistence.RollbackException.class).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}

	@Test
	public void testTransactionRollbackException() {
		javax.transaction.RollbackException exception = new javax.transaction.RollbackException() {
			public Throwable getCause() {
				return new BadRequestException("test");
			}
		};
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		TransactionRollbackExceptionMapper mapper = (TransactionRollbackExceptionMapper) exceptionMapperRegistry.findMapperFor(exception.getClass()).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}
}
