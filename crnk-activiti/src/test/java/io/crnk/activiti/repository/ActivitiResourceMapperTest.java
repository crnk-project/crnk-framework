package io.crnk.activiti.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.mapper.DefaultDateTimeMapper;
import io.crnk.activiti.resource.FormResource;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ActivitiResourceMapperTest {

	private ActivitiResourceMapper mapper = new ActivitiResourceMapper(new TypeParser(), new DefaultDateTimeMapper());

	private TestProcess processResource;

	private TestTask taskResource;

	private TestForm formResource;

	@Before
	public void setup() {
		processResource = new TestProcess();
		processResource.setBooleanValue(true);
		processResource.setStringValue("someString");
		processResource.getNestedBean().setIntValue(13);

		formResource = new TestForm();
		formResource.setApproved(true);

		taskResource = new TestTask();
		taskResource.setForm(formResource);
		taskResource.setSomeIntValue(47);
	}

	@Test
	public void checkProcessInstance() {
		Map<String, Object> variables = mapper.mapToVariables(processResource);
		Assert.assertEquals(3, variables.size());
		Assert.assertEquals("someString", variables.get("stringValue"));
		Assert.assertEquals(13, variables.get("nestedBean.intValue"));
		Assert.assertEquals(Boolean.TRUE, variables.get("booleanValue"));

		TestProcess processCopy = new TestProcess();
		mapper.mapFromVariables(processCopy, variables);
		checkProcessResource(processCopy);

		ProcessInstance processInstance = Mockito.mock(ProcessInstance.class);
		Mockito.when(processInstance.getName()).thenReturn("someProcess");
		Mockito.when(processInstance.getBusinessKey()).thenReturn("someBusinessKey");
		Mockito.when(processInstance.getProcessVariables()).thenReturn(variables);
		TestProcess processResourceCopy = mapper.mapToResource(TestProcess.class, processInstance);
		checkProcessResource(processResourceCopy);
		Assert.assertEquals("someProcess", processResourceCopy.getName());
		Assert.assertEquals("someBusinessKey", processResourceCopy.getBusinessKey());
	}


	@Test
	public void checkTask() {
		Map<String, Object> variables = mapper.mapToVariables(taskResource);
		Assert.assertEquals(1, variables.size());
		Assert.assertEquals(47, variables.get("someIntValue"));

		TestTask taskCopy = new TestTask();
		mapper.mapFromVariables(taskCopy, variables);
		checkTaskResource(taskCopy);

		TaskInfo task = Mockito.mock(TaskInfo.class);
		Mockito.when(task.getName()).thenReturn("someTask");
		Mockito.when(task.getTaskLocalVariables()).thenReturn(variables);
		TestTask taskResourceCopy = mapper.mapToResource(TestTask.class, task);
		checkTaskResource(taskResourceCopy);
		Assert.assertEquals("someTask", taskResourceCopy.getName());

		Map<String, Object> updatedVariables = Mockito.spy(new HashMap<String, Object>());
		Task updatedTask = Mockito.mock(Task.class);
		Mockito.when(updatedTask.getTaskLocalVariables()).thenReturn(updatedVariables);
		taskResourceCopy.setPriority(12);
		taskResourceCopy.setSomeIntValue(5);
		mapper.mapFromResource(taskResourceCopy, updatedTask);
		Mockito.verify(updatedTask, Mockito.times(1)).setPriority(Mockito.eq(12));
		Mockito.verify(updatedVariables, Mockito.times(1)).put(Mockito.eq("someIntValue"), Mockito.eq(Integer.valueOf(5)));
	}


	@Test
	public void checkForm() {
		Map<String, Object> variables = mapper.mapToVariables(formResource);
		Assert.assertEquals(1, variables.size());
		Assert.assertEquals(Boolean.TRUE, variables.get("approved"));

		TestForm resourceCopy = new TestForm();
		mapper.mapFromVariables(resourceCopy, variables);
		checkFormResource(resourceCopy);

		List<FormProperty> formProperties = new ArrayList<>();
		FormProperty formProperty = Mockito.mock(FormProperty.class);
		Mockito.when(formProperty.getId()).thenReturn("approved");
		Mockito.when(formProperty.getValue()).thenReturn("true");
		formProperties.add(formProperty);

		TaskFormData formData = Mockito.mock(TaskFormData.class);
		Mockito.when(formData.getFormProperties()).thenReturn(formProperties);
		Task task = Mockito.mock(Task.class);
		Mockito.when(task.getId()).thenReturn("someTask");
		Mockito.when(formData.getTask()).thenReturn(task);
		TestForm formCopy = mapper.mapToResource(TestForm.class, formData);
		checkFormResource(formCopy);
		Assert.assertEquals("someTask", formCopy.getId());
	}


	private void checkProcessResource(TestProcess resource) {
		Assert.assertEquals("someString", resource.getStringValue());
		Assert.assertTrue(resource.isBooleanValue());
		Assert.assertEquals(13, resource.getNestedBean().getIntValue());
	}

	private void checkFormResource(TestForm resource) {
		Assert.assertTrue(resource.isApproved());
	}

	private void checkTaskResource(TestTask resource) {
		Assert.assertEquals(47, resource.getSomeIntValue());
	}

	@JsonApiResource(type = "test/form")
	public static class TestForm extends FormResource {

		private boolean approved;

		public boolean isApproved() {
			return approved;
		}

		public void setApproved(boolean approved) {
			this.approved = approved;
		}
	}


	@JsonApiResource(type = "test/task")
	public static class TestTask extends TaskResource {

		private int someIntValue;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
		private TestForm form;

		public int getSomeIntValue() {
			return someIntValue;
		}

		public void setSomeIntValue(int someIntValue) {
			this.someIntValue = someIntValue;
		}

		public TestForm getForm() {
			return form;
		}

		public void setForm(TestForm form) {
			this.form = form;
		}
	}

	@JsonApiResource(type = "test/process")
	public static class TestProcess extends ProcessInstanceResource {

		private String stringValue;

		private boolean booleanValue;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
		private TestTask testTask;

		private TestNestedBean nestedBean = new TestNestedBean();

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		public boolean isBooleanValue() {
			return booleanValue;
		}

		public void setBooleanValue(boolean booleanValue) {
			this.booleanValue = booleanValue;
		}

		public TestNestedBean getNestedBean() {
			return nestedBean;
		}

		public void setNestedBean(TestNestedBean nestedBean) {
			this.nestedBean = nestedBean;
		}
	}

	public static class TestNestedBean {

		private int intValue;

		public int getIntValue() {
			return intValue;
		}

		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}
	}

}
