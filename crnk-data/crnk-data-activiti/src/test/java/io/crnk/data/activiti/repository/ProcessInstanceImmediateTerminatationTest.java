package io.crnk.data.activiti.repository;

import io.crnk.data.activiti.example.model.ImmediateTerminatationProcessInstance;
import io.crnk.data.activiti.internal.repository.ProcessInstanceResourceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstanceImmediateTerminatationTest extends ActivitiTestBase {

    private ProcessInstanceResourceRepository<ImmediateTerminatationProcessInstance> processRepository;

    @Before
    public void setup() {
        super.setup();

        processRepository =
                (ProcessInstanceResourceRepository<ImmediateTerminatationProcessInstance>) boot.getResourceRegistry().getEntry
                        (ImmediateTerminatationProcessInstance.class).getResourceRepository().getResourceRepository();

    }

    @Test
    public void test() {
        ImmediateTerminatationProcessInstance processInstance = new ImmediateTerminatationProcessInstance();
        processInstance.setValue("test");

        ImmediateTerminatationProcessInstance createdProcessInstance = processRepository.create(processInstance);
        Assert.assertNotNull(createdProcessInstance);
        Assert.assertTrue(createdProcessInstance.isEnded());
        Assert.assertFalse(createdProcessInstance.isSuspended());
        Assert.assertNull(createdProcessInstance.getDescription());
        Assert.assertEquals("quickStartEvent", createdProcessInstance.getActivityId());
        Assert.assertEquals("test", createdProcessInstance.getValue());

        Assert.assertNotNull(createdProcessInstance.getId());
    }
}
