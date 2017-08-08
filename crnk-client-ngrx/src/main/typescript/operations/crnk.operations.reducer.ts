import {Action} from "@ngrx/store";
import "rxjs/add/observable/of";
import "rxjs/add/operator/catch";
import "rxjs/add/operator/concatAll";
import "rxjs/add/operator/do";
import "rxjs/add/operator/mapTo";
import "rxjs/add/operator/mergeMap";
import "rxjs/add/operator/switchMap";
import "rxjs/add/operator/switchMapTo";
import "rxjs/add/operator/take";
import "rxjs/add/operator/toArray";
import "rxjs/add/operator/withLatestFrom";
import {NgrxJsonApiStore, NgrxJsonApiStoreReducer, StoreResource} from "ngrx-json-api";
import {OperationActionTypes} from "./crnk.operations.actions";

import {getPendingChanges} from './crnk.operations.utils';

export function OperationsStoreReducer(state: NgrxJsonApiStore, action: Action) {
	switch (action.type) {
		case OperationActionTypes.OPERATIONS_INIT:
			const pending: Array<StoreResource> = getPendingChanges(state);
			const newState = Object.assign({}, state, {isApplying: state.isApplying + 1});
			for (const pendingChange of pending) {
				if (pendingChange.state === 'CREATED') {
					newState.isCreating++;
				}
				else if (pendingChange.state === 'UPDATED') {
					newState.isUpdating++;
				}
				else if (pendingChange.state === 'DELETED') {
					newState.isDeleting++;
				}
				else {
					throw new Error('unknown state ' + pendingChange.state);
				}
			}
			return newState;
		default:
			// TODO Consider compose?
			return NgrxJsonApiStoreReducer(state, action);
	}
};
