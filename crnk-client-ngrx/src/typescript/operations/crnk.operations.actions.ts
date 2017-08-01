import {Action} from "@ngrx/store";


export const OperationActionTypes = {
	OPERATIONS_INIT: 'OPERATIONS_INIT',
	OPERATIONS_SUCCESS: 'OPERATIONS_SUCCESS',
	OPERATIONS_FAIL: 'OPERATIONS_FAIL',
};

export class OperationsInitAction implements Action {
	type = OperationActionTypes.OPERATIONS_INIT;
}

