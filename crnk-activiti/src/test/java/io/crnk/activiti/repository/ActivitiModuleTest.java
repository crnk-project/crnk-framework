package io.crnk.activiti.repository;

import io.crnk.activiti.ActivitiModule;
import io.crnk.activiti.example.ApprovalTestApplication;
import io.crnk.activiti.example.model.ApproveForm;
import io.crnk.activiti.example.model.ApproveTask;
import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.core.module.Module;
import org.junit.Assert;
import org.junit.Test;

public class ActivitiModuleTest extends ActivitiTestBase {

	@Test
	public void checkGetRepositories() {
		ActivitiModule module = boot.getModuleRegistry().getModule(ActivitiModule.class).get();

		Assert.assertNotNull(module.getTaskRepository(ApproveTask.class));
		Assert.assertNotNull(module.getFormRepository(ApproveForm.class));
		Assert.assertNotNull(module.getProcessInstanceRepository(ScheduleApprovalProcessInstance.class));
	}


	@Override
	protected Module createActivitiModule() {
		return ApprovalTestApplication.createActivitiModule(processEngine);
	}

}
