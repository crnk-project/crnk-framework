package io.crnk.spring;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.spring.exception.BeanExceptionMapper;
import io.crnk.spring.exception.SpringExceptionModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

public class SpringExceptionMapperTest {

	private CrnkBoot boot;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new SpringExceptionModule());
		boot.boot();
	}

	@Test
	public void testBeanCreationExceptionUnwrapped() {
		BeanCreationException exception = new BeanCreationException("someMessage", new BadRequestException("test"));
		ExceptionMapperRegistry exceptionMapperRegistry = boot.getExceptionMapperRegistry();
		BeanExceptionMapper mapper = (BeanExceptionMapper) exceptionMapperRegistry.findMapperFor(BeanCreationException.class).get();
		ErrorResponse response = mapper.toErrorResponse(exception);
		ErrorData errorData = response.getErrors().iterator().next();
		Assert.assertEquals(Integer.toString(HttpStatus.BAD_REQUEST_400), errorData.getStatus());
		Assert.assertEquals("test", errorData.getDetail());
	}
}
