package io.crnk.data.activiti.example.approval;

import io.crnk.data.activiti.example.model.ScheduleApprovalValues;
import io.crnk.test.mock.models.Schedule;

/**
 * You may consider a tool like MapStruct in real life scenarios with more attributes.
 */
public class ApprovalMapper {

	public ApprovalValues mapValues(Object object) {
		if (object instanceof Schedule) {
			return mapValues((Schedule) object);
		}
		throw new IllegalStateException();
	}

	public void unmapValues(ApprovalValues attributes, Object object) {
		if (object instanceof Schedule) {
			unmapValues((ScheduleApprovalValues) attributes, (Schedule) object);
		} else {
			throw new IllegalStateException();
		}
	}


	protected ScheduleApprovalValues mapValues(Schedule entity) {
		ScheduleApprovalValues values = new ScheduleApprovalValues();
		values.setName(entity.getName());
		return values;
	}


	protected void unmapValues(ScheduleApprovalValues attributes, Schedule entity) {
		entity.setName(attributes.getName());
	}
}
