package io.crnk.data.activiti.repository;

import io.crnk.data.activiti.ActivitiModule;
import io.crnk.data.activiti.example.model.ApproveForm;
import io.crnk.data.activiti.example.model.ApproveTask;
import io.crnk.data.activiti.example.model.ScheduleApprovalProcessInstance;
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

}
