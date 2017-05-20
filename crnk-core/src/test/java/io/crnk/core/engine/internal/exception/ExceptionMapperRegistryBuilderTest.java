package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.handlers.NoAnnotationExceptionMapper;
import io.crnk.core.engine.error.handlers.SomeExceptionMapper;
import io.crnk.core.exception.CrnkMappableException;
import io.crnk.core.exception.InvalidResourceException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionMapperRegistryBuilderTest {

	private final ExceptionMapperRegistryBuilder builder = new ExceptionMapperRegistryBuilder();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldThrowExceptionWhenAnnotatedClassIsNotImplementingJsonMapper() throws Exception {
		expectedException.expect(InvalidResourceException.class);
		builder.build("io.crnk.core.engine.error.badhandler");
	}

	@Test
	public void shouldContainDefaultExceptionMapper() throws Exception {
		ExceptionMapperRegistry registry = builder.build("io.crnk.core.engine.error.handlers");
		assertThat(registry.getExceptionMappers())
				.isNotNull()
				.extracting("exceptionClass")
				.contains(CrnkMappableException.class)
				.contains(SomeExceptionMapper.SomeException.class)
				.contains(IllegalArgumentException.class);
	}

	@Test
	public void shouldContainScannedExceptionMapper() throws Exception {
		ExceptionMapperRegistry registry = builder.build("io.crnk.core.engine.error.handlers");
		assertThat(registry.getExceptionMappers())
				.isNotNull()
				.extracting("exceptionClass")
				.contains(SomeExceptionMapper.SomeException.class)
				.contains(IllegalArgumentException.class);
	}

	@Test
	public void shouldNotContainNotAnnotatedExceptionMapper() throws Exception {
		ExceptionMapperRegistry registry = builder.build("io.crnk.core.engine.error.handlers");
		assertThat(registry.getExceptionMappers())
				.isNotNull()
				.extracting("exceptionClass")
				.doesNotContain(NoAnnotationExceptionMapper.ShouldNotAppearException.class);
	}


	@Test
	public void shouldContainScannedExceptionMapperWhenMultiplePaths() throws Exception {
		ExceptionMapperRegistry registry = builder.build("io.crnk.core.engine.error.handlers,io.crnk.core.engine.error.handlers");
		assertThat(registry.getExceptionMappers())
				.isNotNull()
				.extracting("exceptionClass")
				.contains(SomeExceptionMapper.SomeException.class)
				.contains(IllegalArgumentException.class);
	}

}
