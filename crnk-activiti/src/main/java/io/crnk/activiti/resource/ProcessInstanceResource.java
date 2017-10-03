package io.crnk.activiti.resource;

import io.crnk.core.resource.annotations.JsonApiField;

public class ProcessInstanceResource extends ExecutionResource {

	private String businessKey;

	@JsonApiField(patchable = false)
	private String processDefinitionKey;

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}
}
