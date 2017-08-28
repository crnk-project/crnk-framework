import {Action} from "@ngrx/store";


export const OperationActionTypes = {
	OPERATIONS_INIT: 'OPERATIONS_INIT'
};

export class OperationsInitAction implements Action {
	type = OperationActionTypes.OPERATIONS_INIT;
}

