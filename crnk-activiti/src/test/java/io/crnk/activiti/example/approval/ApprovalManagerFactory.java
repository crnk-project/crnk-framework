package io.crnk.activiti.example.approval;

@Deprecated // TODO find a better solution without singleton
public class ApprovalManagerFactory {

	private static ApprovalManager instance;

	public ApprovalManager getInstance() {
		if (instance == null) {
			instance = new ApprovalManager();
		}
		return instance;
	}

}
