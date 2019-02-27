package io.crnk.activiti.example.model;

import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "approval/termination")
public class ImmediateTerminatationProcessInstance extends ProcessInstanceResource {

}