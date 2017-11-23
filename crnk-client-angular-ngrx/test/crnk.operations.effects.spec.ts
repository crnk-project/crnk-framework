import {getTestBed, TestBed} from '@angular/core/testing';

import 'rxjs/add/operator/merge';
import {ActionReducerMap, Store, StoreModule} from '@ngrx/store';


import {ApiApplyInitAction, NgrxJsonApiService, NgrxJsonApiStore, NgrxJsonApiZone, Resource} from 'ngrx-json-api';

import {NGRX_JSON_API_CONFIG, selectorsFactory, serviceFactory} from 'ngrx-json-api/src/module';

import {initialNgrxJsonApiState} from 'ngrx-json-api/src/reducers';


import {updateStoreDataFromPayload,} from 'ngrx-json-api/src/utils';

import {ApiApplySuccessAction, NgrxJsonApiActionTypes} from 'ngrx-json-api/src/actions';

import {testPayload} from './crnk.operations.spec.utils'

import {MOCK_JSON_API_PROVIDERS} from './crnk.operations.spec.mock';
import {OperationsEffects} from '../operations/crnk.operations.effects';
import {MockBackend, MockConnection} from '@angular/http/testing';
import {BaseRequestOptions, Http, RequestMethod, Response, ResponseOptions, XHRBackend} from '@angular/http';
import {provideMockActions} from '@ngrx/effects/testing';
import {EffectsModule} from '@ngrx/effects';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {ReplaySubject} from 'rxjs/ReplaySubject';
import {NgrxJsonApiSelectors} from 'ngrx-json-api/src/selectors';


export const testReducer: ActionReducerMap<any> = {};

let initialState = {
	NgrxJsonApi: {
		zones: {
			default: {
				...{},
				...initialNgrxJsonApiState,
				...{
					data: updateStoreDataFromPayload({}, testPayload),
				}
			}
		}
	}
};

@NgModule({
	imports: [

		HttpClientTestingModule,
		HttpClientModule
	],
	providers: [],
})
export class TestingModule {
}

describe('OperationsEffects', () => {
	let effects: OperationsEffects;
	let mockBackend: MockBackend;
	let store: Store<any>;

	let actions: ReplaySubject<any>;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [
				TestingModule,
				StoreModule.forRoot(testReducer, {initialState: initialState}),
				EffectsModule.forRoot([OperationsEffects])
			],
			providers: [
				...MOCK_JSON_API_PROVIDERS,
				{
					provide: NgrxJsonApiSelectors,
					useFactory: selectorsFactory,
					deps: [NGRX_JSON_API_CONFIG]
				},
				{
					provide: NgrxJsonApiService,
					useFactory: serviceFactory,
					deps: [Store, NgrxJsonApiSelectors]
				},
				{
					provide: NGRX_JSON_API_CONFIG,
					useValue: {
						storeLocation: 'api',
						apiUrl: 'api'
					}
				},
				provideMockActions(() => actions),
				MockBackend,
				BaseRequestOptions,
				{
					provide: Http,
					deps: [MockBackend, BaseRequestOptions],
					useFactory:
						(backend: XHRBackend, defaultOptions: BaseRequestOptions) => {
							return new Http(backend, defaultOptions);
						}
				}

			]
		});

	});


	let returnError;

	beforeEach(() => {
		mockBackend = getTestBed().get(MockBackend);
		effects = getTestBed().get(OperationsEffects);
		store = getTestBed().get(Store);

		returnError = false;
		initMockHttpResponses();
	});


	it('should do nothing when store in sync', () => {
		let res;
		actions = new ReplaySubject(1);
		actions.next(new ApiApplyInitAction({}, 'default'));
		effects.applyResources$.subscribe(result => {
			res = result;
			expect(result).toEqual(new ApiApplySuccessAction([], 'default'));
		});
		expect(res).toBeDefined();
	});

	it('should post created resource', () => {
		let res;
		let zone = store['source']['_value']['NgrxJsonApi']['zones']['default'] as NgrxJsonApiZone;
		zone.data['Article']['1'].state = 'CREATED';

		actions = new ReplaySubject(1);
		actions.next(new ApiApplyInitAction({}, 'default'));

		effects.applyResources$.subscribe(result => {
			res = result;

			expect(result.type).toEqual(NgrxJsonApiActionTypes.API_APPLY_SUCCESS);
			expect(result.payload[0].type).toEqual(NgrxJsonApiActionTypes.API_POST_SUCCESS);
			const responseResource = result.payload[0].payload.jsonApiData.data as Resource;
			expect(responseResource.id).toEqual('1');
			expect(responseResource.type).toEqual('Article');
			expect(responseResource.attributes['title']).toEqual('Updated Title');
		});
		expect(res).toBeDefined();
	});

	it('should patch updated resource', () => {
		let res;

		let zone = store['source']['_value']['NgrxJsonApi']['zones']['default'] as NgrxJsonApiZone;
		zone.data['Article']['1'].state = 'UPDATED';

		actions = new ReplaySubject(1);
		actions.next(new ApiApplyInitAction({}, 'default'));

		effects.applyResources$.subscribe(result => {
			res = result;

			expect(result.type).toEqual(NgrxJsonApiActionTypes.API_APPLY_SUCCESS);
			expect(result.payload[0].type).toEqual(NgrxJsonApiActionTypes.API_PATCH_SUCCESS);
			const responseResource = result.payload[0].payload.jsonApiData.data as Resource;
			expect(responseResource.id).toEqual('1');
			expect(responseResource.type).toEqual('Article');
			expect(responseResource.attributes['title']).toEqual('Updated Title');
		});
		expect(res).toBeDefined();
	});


	it('should  fail to update resource', () => {
		let res;

		returnError = true;

		let zone = store['source']['_value']['NgrxJsonApi']['zones']['default'] as NgrxJsonApiZone;
		zone.data['Article']['1'].state = 'UPDATED';

		actions = new ReplaySubject(1);
		actions.next(new ApiApplyInitAction({}, 'default'));

		effects.applyResources$.subscribe(result => {
			res = result;

			expect(result.type).toEqual(NgrxJsonApiActionTypes.API_APPLY_FAIL);
			expect(result.payload[0].type).toEqual(NgrxJsonApiActionTypes.API_PATCH_FAIL);
			const responseResource = result.payload[0].payload.jsonApiData.data as Resource;
			expect(responseResource.id).toEqual('1');
			expect(responseResource.type).toEqual('Article');
			expect(responseResource.attributes['title']).toEqual('Updated Title');
		});
		expect(res).toBeDefined();
	});


	it('should delete resource', () => {
		let res;

		let zone = store['source']['_value']['NgrxJsonApi']['zones']['default'] as NgrxJsonApiZone;
		zone.data['Article']['1'].state = 'DELETED';

		actions = new ReplaySubject(1);
		actions.next(new ApiApplyInitAction({}, 'default'));

		effects.applyResources$.subscribe(result => {
			res = result;

			expect(result.type).toEqual(NgrxJsonApiActionTypes.API_APPLY_SUCCESS);
			expect(result.payload[0].type).toEqual(NgrxJsonApiActionTypes.API_DELETE_SUCCESS);
		});
		expect(res).toBeDefined();
	});

	function initMockHttpResponses() {
		mockBackend.connections.subscribe((connection: MockConnection) => {
			expect(connection.request.method).toBe(RequestMethod.Patch);

			let response;
			if (returnError) {
				response = [
					{
						status: 400,
						data: {
							type: 'Article',
							id: '1',
							attributes: {
								title: 'Updated Title'
							},
							errors: [
								{
									code: 'someErrorCode'
								}
							]
						}
					}
				];
			} else {
				response = [
					{
						status: 200,
						data: {
							type: 'Article',
							id: '1',
							attributes: {
								title: 'Updated Title'
							}
						}
					}
				];
			}

			connection.mockRespond(new Response(new ResponseOptions({
				body: response
			})));
		});
	}

});
