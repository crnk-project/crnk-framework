package io.crnk.activiti.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.crnk.activiti.resource.ExecutionResource;
import io.crnk.activiti.resource.FormResource;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.resource.annotations.JsonApiRelation;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.ClassUtils;

public class ActivitiResourceMapper {

	/**
	 * contains computed resource attributes that have no equivalent in Activiti.
	 */
	private static final Collection<String> IGNORED_ATTRIBUTES = Arrays.asList("completed");

	private TypeParser typeParser;

	private DateTimeMapper dateTimeMapper;

	protected ActivitiResourceMapper() {
	}

	public ActivitiResourceMapper(TypeParser typeParser, DateTimeMapper dateTimeMapper) {
		this.typeParser = typeParser;
		this.dateTimeMapper = dateTimeMapper;
	}

	public <T extends ProcessInstanceResource> T mapToResource(Class<T> resourceClass, ProcessInstance processInstance) {
		T resource = io.crnk.core.engine.internal.utils.ClassUtils.newInstance(resourceClass);
		Map<String, Object> processVariables = processInstance.getProcessVariables();
		copyInternal(resource, null, processVariables, true, Optional.of(processInstance));
		return resource;
	}

	public <T extends FormResource> T mapToResource(Class<T> resourceClass, TaskFormData formData) {
		T resource = io.crnk.core.engine.internal.utils.ClassUtils.newInstance(resourceClass);
		Map<String, Object> formPropertyMap = new HashMap<>();
		List<FormProperty> formProperties = formData.getFormProperties();
		for (FormProperty formProperty : formProperties) {
			formPropertyMap.put(formProperty.getId(), formProperty.getValue());
		}
		copyInternal(resource, null, formPropertyMap, true, Optional.empty());
		resource.setId(formData.getTask().getId());
		return resource;
	}

	public void mapFromResource(TaskResource resource, Task task) {
		Map<String, Object> variables = task.getTaskLocalVariables();
		copyInternal(resource, null, variables, false, Optional.of(task));
	}

	public <T extends TaskResource> T mapToResource(Class<T> resourceClass, TaskInfo task) {
		T resource = io.crnk.core.engine.internal.utils.ClassUtils.newInstance(resourceClass);
		Map<String, Object> variables = task.getTaskLocalVariables();
		copyInternal(resource, null, variables, true, Optional.of(task));
		return resource;
	}


	public Map<String, Object> mapToVariables(Object resource) {
		Map<String, Object> variables = new HashMap<>();
		copyInternal(resource, null, variables, false, Optional.empty());
		return variables;
	}


	public Map<String, Object> mapFromVariables(Object resource, Map<String, Object> variables) {
		copyInternal(resource, null, variables, true, Optional.empty());
		return variables;
	}


	private void copyInternal(Object resource, String prefix, Map<String, Object> variables, boolean toResource,
			Optional<Object> variableHolder) {
		BeanInformation beanInformation = BeanInformation.get(resource.getClass());
		for (String attributeName : beanInformation.getAttributeNames()) {
			BeanAttributeInformation attribute = beanInformation.getAttribute(attributeName);
			if (attribute.getAnnotation(JsonApiRelation.class).isPresent() || IGNORED_ATTRIBUTES.contains(attributeName)) {
				// handled separately
				continue;
			}

			if (isStaticField(attribute)) {
				copyStaticField(resource, attributeName, variableHolder, toResource);
			}
			else {
				copyDynamicField(resource, attribute, variableHolder, variables, prefix, toResource);
			}
		}
	}

	private boolean isStaticField(BeanAttributeInformation attribute) {
		Package activitiPackage = ExecutionResource.class.getPackage();
		Method getter = attribute.getGetter();
		Class declaringClass = getter.getDeclaringClass();

		if (declaringClass.getPackage().equals(activitiPackage)) {
			return true;
		}
		// processInstanceId as exception since not on task, but defined as relationship on custom resource
		return attribute.getName().equals("processInstanceId");
	}

	private void copyStaticField(Object resource, String attributeName, Optional<Object> activitiBean, boolean toResource) {
		PreconditionUtil.assertNotNull("cannot process nested holder structures", activitiBean);
		// map fields
		if (activitiBean.isPresent()) {
			BeanInformation activitiBeanInformation = BeanInformation.get(activitiBean.get().getClass());
			if (toResource) {
				Object value = PropertyUtils.getProperty(activitiBean.get(), attributeName);
				PropertyUtils.setProperty(resource, attributeName, mapValue(value));
			}
			else {

				BeanAttributeInformation attribute = activitiBeanInformation.getAttribute(attributeName);
				if (attribute != null && attribute.getSetter() != null) {
					Object value = PropertyUtils.getProperty(resource, attributeName);
					PropertyUtils.setProperty(activitiBean.get(), attributeName, unmapValue(value));
				}
			}
		}
	}

	private void copyDynamicField(Object resource, BeanAttributeInformation attribute, Optional<Object> variableHolder,
			Map<String, Object> variables, String prefix, boolean toResource) {
		String attributeName = attribute.getName();
		Method getter = attribute.getGetter();
		try {
			// add variables
			String key = prefix != null ? prefix + "." + attributeName : attributeName;
			if (isPrimitive(getter.getReturnType())) {
				if (toResource) {
					Object value = variables.get(key);
					if (value != null) {
						Object mappedValue = mapValue(value, attribute.getImplementationClass());
						PropertyUtils.setProperty(resource, attributeName, mappedValue);
					}
				}
				else {
					Object value = getter.invoke(resource);
					variables.put(key, value);
				}
			}
			else {
				Object childResource = getter.invoke(resource);
				if (childResource == null) {
					childResource =
							io.crnk.core.engine.internal.utils.ClassUtils.newInstance(attribute.getGetter().getReturnType());
					PropertyUtils.setProperty(resource, attributeName, childResource);
				}

				Optional childVariableHolder = null; // not necessary/supported
				if (childResource != null) {
					copyInternal(childResource, key, variables, toResource, childVariableHolder);
				}
			}
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}


	private Object mapValue(Object value) {
		if (value instanceof Date) {
			Date date = (Date) value;
			return dateTimeMapper.toOffsetDateTime(date);
		}
		return value;
	}

	private Object unmapValue(Object value) {
		if (value instanceof OffsetDateTime) {
			OffsetDateTime date = (OffsetDateTime) value;
			return Date.from(date.toInstant());
		}
		return value;
	}


	private Object mapValue(Object value, Class<?> type) {
		if (value instanceof String && type != String.class) {
			return typeParser.parse((String) value, type);
		}
		return mapValue(value);
	}

	private boolean isPrimitive(Class clazz) {
		return clazz == String.class || ClassUtils.isPrimitiveOrWrapper(clazz) || LocalDate.class.getPackage().equals(clazz
				.getPackage()) || clazz.isEnum() || clazz == UUID.class;
	}

}
