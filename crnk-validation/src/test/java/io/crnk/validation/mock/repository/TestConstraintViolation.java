package io.crnk.validation.mock.repository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;

public class TestConstraintViolation implements ConstraintViolation<Object> {

	private Object rootBean;
	private Object leafBean;

	public TestConstraintViolation(Object rootBean, Object leafBean) {

	}

	@Override
	public String getMessage() {
		return "testMsg";
	}

	@Override
	public String getMessageTemplate() {
		return "testTemplate";
	}

	@Override
	public Object getRootBean() {
		return rootBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Object> getRootBeanClass() {
		return (Class<Object>) rootBean.getClass();
	}

	@Override
	public Object getLeafBean() {
		return leafBean;
	}

	@Override
	public Object[] getExecutableParameters() {
		return null;
	}

	@Override
	public Object getExecutableReturnValue() {
		return null;
	}

	@Override
	public Path getPropertyPath() {
		return null;
	}

	@Override
	public Object getInvalidValue() {
		return "x";
	}

	@Override
	public ConstraintDescriptor<?> getConstraintDescriptor() {
		return null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Object unwrap(Class type) {
		return null;
	}

}
