import * as _ from 'lodash';

import {Injectable, Injector} from '@angular/core';
import {Action, Store} from '@ngrx/store';
import {Actions, Effect} from '@ngrx/effects';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/concatAll';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/mapTo';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/switchMapTo';
import 'rxjs/add/operator/take';
import 'rxjs/add/operator/toArray';
import 'rxjs/add/operator/withLatestFrom';
import {Headers, Http, Request, RequestMethod, RequestOptions} from '@angular/http';
import {
	ApiApplyInitAction,
	Document, NgrxJsonApiActionTypes, NgrxJsonApiService, NgrxJsonApiStore, Query, Resource, ResourceError,
	StoreResource
} from 'ngrx-json-api';

import {
	ApiApplyFailAction,
	ApiApplySuccessAction,
	ApiDeleteFailAction,
	ApiDeleteSuccessAction,
	ApiPatchFailAction,
	ApiPatchSuccessAction,
	ApiPostFailAction,
	ApiPostSuccessAction,
} from 'ngrx-json-api/src/actions';

import {getPendingChanges} from 'ngrx-json-api/src/utils';
import {NgrxJsonApi} from 'ngrx-json-api/src/api';


interface Operation {

	op: string;

	path: string;

	value?: Resource;

}

interface OperationResponse extends Document {

	status: number;
}


@Injectable()
export class OperationsEffects {

	public headers: Headers = new Headers({
		'Content-Type': 'application/json-patch+json',
		'Accept': 'application/json-patch+json'
	});

	@Effect() applyResources$ = this.actions$
		.ofType(NgrxJsonApiActionTypes.API_APPLY_INIT)
		.withLatestFrom(this.store.select(it => {
			let feature = it['NgrxJsonApi'];
			return feature ? feature['api'] : undefined;
		}), (action, ngrxstore: NgrxJsonApiStore) => {
			let payload = (action as ApiApplyInitAction).payload;
			const pending: Array<StoreResource> = getPendingChanges(ngrxstore.data, payload.ids, payload.include, false);
			return pending;
		})
		.flatMap(pending => {
			if (pending.length === 0) {
				return Observable.of(new ApiApplySuccessAction([]));
			}

			const operations: Array<Operation> = [];
			for (const pendingChange of pending) {
				operations.push(this.toOperation(pendingChange));
			}

			const requestOptions = new RequestOptions({
				method: RequestMethod.Patch,
				url: this.ngrxJsonApi['selectors']['config']['apiUrl'] + '/operations/',
				body: JSON.stringify(operations)
			});

			const request = new Request(requestOptions.merge({
				headers: this.headers
			}));

			return this.http.request(request)
				.map(res => res.json() as Array<OperationResponse>)
				.map(operationResponses => {
					if (!_.isArray(operationResponses)) {
						throw new Error('expected array as operations response');
					}

					const actions: Array<Action> = [];
					for (const index of Object.keys(operationResponses)) {
						const operationResponse = operationResponses[index];
						const pendingChange = pending[index];
						actions.push(this.toResponseAction(pendingChange, operationResponse));
					}
					return this.toApplyAction(actions);
				})
				.catch(errorResponse => {
					// transform http to json api error
					const error: ResourceError = {
						status: errorResponse.status.toString(),
						code: errorResponse.statusText
					};
					const errors: Array<ResourceError> = [error];

					const actions: Array<Action> = [];
					for (const pendingChange of pending) {
						const operationResponse: OperationResponse = {
							errors: errors,
							status: 500
						} as OperationResponse;
						actions.push(this.toResponseAction(pendingChange, operationResponse));
					}
					return Observable.of(new ApiApplyFailAction(actions));
				});
		});



	constructor(
		private actions$: Actions,
		private store: Store<any>,
		private http: Http,
		private ngrxJsonApi: NgrxJsonApiService
	) {
		// disable default implementation
		this.ngrxJsonApi['selectors']['config']['applyEnabled'] = false;
	}

	private toOperation(pendingChange: StoreResource): Operation {
		if (pendingChange.state === 'CREATED') {
			return {
				op: 'POST',
				path: pendingChange.type,
				value: {
					id: pendingChange.hasTemporaryId ? undefined : pendingChange.id,
					type: pendingChange.type,
					attributes: pendingChange.attributes,
					relationships: pendingChange.relationships
				}
			};
		}
		else if (pendingChange.state === 'UPDATED') {
			return {
				op: 'PATCH',
				path: pendingChange.type + '/' + pendingChange.id,
				value: {
					id: pendingChange.id,
					type: pendingChange.type,
					attributes: pendingChange.attributes,
					relationships: pendingChange.relationships
				}
			};
		}
		else if (pendingChange.state === 'DELETED') {
			return {
				op: 'DELETE',
				path: pendingChange.type + '/' + pendingChange.id
			};
		}
		else {
			throw new Error('unknown state ' + pendingChange.state);
		}
	}

	private toApplyAction(actions: Array<Action>): any {
		for (const action of actions) {
			if (action.type === NgrxJsonApiActionTypes.API_POST_FAIL
				|| action.type === NgrxJsonApiActionTypes.API_PATCH_FAIL
				|| action.type === NgrxJsonApiActionTypes.API_DELETE_FAIL) {
				return new ApiApplyFailAction(actions);
			}
		}
		return new ApiApplySuccessAction(actions);
	}

	private toResponseAction(pendingChange: StoreResource, operationResponse: OperationResponse): Action {
		const success = operationResponse.status >= 200 && operationResponse.status < 300;

		const jsonApiData: Document = _.omit(operationResponse, ['status']) as Document;

		const query: Query = {
			type: pendingChange.type,
			id: pendingChange.id
		};
		if (pendingChange.state === 'CREATED') {
			if (success) {
				// get generated primary key
				query.id = jsonApiData.data['id'];
				if (!query.id) {
					throw new Error('id not returned for operation');
				}
				return new ApiPostSuccessAction({
					jsonApiData: jsonApiData,
					query: query
				});
			}
			else {
				return new ApiPostFailAction({
					jsonApiData: jsonApiData,
					query: query
				});
			}
		}
		else if (pendingChange.state === 'UPDATED') {
			if (success) {
				return new ApiPatchSuccessAction({
					jsonApiData: jsonApiData,
					query: query
				});
			}
			else {
				return new ApiPatchFailAction({
					jsonApiData: jsonApiData,
					query: query
				});
			}
		}
		else if (pendingChange.state === 'DELETED') {
			if (success) {
				return new ApiDeleteSuccessAction({
					query: query
				});
			}
			else {
				return new ApiDeleteFailAction({
					jsonApiData: jsonApiData,
					query: query
				});
			}
		}
		else {
			throw new Error('unknown state ' + pendingChange.state);
		}
	}
}
