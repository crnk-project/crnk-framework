import {getTestBed, TestBed} from '@angular/core/testing';

import "rxjs/add/operator/merge";
import {Store, StoreModule} from '@ngrx/store';

import {EffectsRunner, EffectsTestingModule} from '@ngrx/effects/testing';

import {
	ApiApplySuccessAction,
	initialNgrxJsonApiState,
	NGRX_JSON_API_CONFIG,
	NgrxJsonApiSelectors,
	NgrxJsonApiStore,
	selectorsFactory,
	updateStoreDataFromPayload
} from 'ngrx-json-api';

import {testPayload} from './crnk.operations.spec.utils'

import {MOCK_JSON_API_PROVIDERS} from './crnk.operations.spec.mock';

import {OperationsStoreReducer} from "./crnk.operations.reducer";
import {OperationsEffects} from "./crnk.operations.effects";
import {MockBackend, MockConnection} from "@angular/http/testing";
import {BaseRequestOptions, Http, RequestMethod, Response, ResponseOptions, XHRBackend} from "@angular/http";
import {OperationsInitAction} from "./crnk.operations.actions";

describe('OperationsEffects', () => {
	let runner: EffectsRunner;
	let effects: OperationsEffects;
	let mockBackend: MockBackend;
	let store: Store<any>;

	beforeEach(() => {
		let initialState = {
			api: Object.assign({}, initialNgrxJsonApiState, {
				data: updateStoreDataFromPayload({}, testPayload),
			},)
		};
		TestBed.configureTestingModule({
			imports: [
				EffectsTestingModule,
				StoreModule.provideStore({api: OperationsStoreReducer}, initialState),
			],
			providers: [

				OperationsEffects,

				...MOCK_JSON_API_PROVIDERS,
				{
					provide: NgrxJsonApiSelectors,
					useFactory: selectorsFactory,
					deps: [NGRX_JSON_API_CONFIG]
				},
				{
					provide: NGRX_JSON_API_CONFIG,
					useValue: {
						storeLocation: 'api',
						apiUrl: 'api'
					}
				},
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

	beforeEach(() => {
		mockBackend = getTestBed().get(MockBackend);
		effects = getTestBed().get(OperationsEffects);
		runner = getTestBed().get(EffectsRunner);
		store = getTestBed().get(Store);
		console.log("hi", store);

		initMockHttpResponses();
	});

	let successPayload = {
		jsonApiData: {
			data: {
				type: 'SUCCESS'
			}
		},
		query: {
			type: 'SUCCESS'
		}
	};
	let failPayload = {
		jsonApiData: {
			data: {
				type: 'FAIL'
			}
		},
		query: {
			type: 'FAIL'
		}
	};
	let successQuery = {
		query: {
			type: 'SUCCESS'
		}
	};
	let failQuery = {
		query: {
			type: 'FAIL'
		}
	};

	it('should do nothing when store in sync', () => {
		let res;
		runner.queue(new OperationsInitAction());
		effects.applyResources$.flatMap(it => it).subscribe(result => {
			res = result;
			console.log("got result", result);

			expect(result).toEqual(new ApiApplySuccessAction([]));
		});
		expect(res).toBeDefined();
	});

	fit('should post created resource', () => {
		let res;

		let api = store['source']['_value']['api'] as NgrxJsonApiStore;
		api.data['Article']['1'].state = 'CREATED';

		console.log(store['source']['_value']['api']);

		console.log("store", store);
		runner.queue(new OperationsInitAction());
		effects.applyResources$.flatMap(it => it).subscribe(result => {
			res = result;
			console.log("got result", result);

			expect(result).toEqual(new ApiApplySuccessAction([]));
		});
		expect(res).toBeDefined();
	});

	function initMockHttpResponses() {
		const mockMovie1 = {
			id: '1',
			type: 'movie',
			attributes: {
				fullPlot: null,
				year: '1964',
				imdbId: 'tt0984034',
				metacritic: null,
				rating: null,
				runtime: '10 min',
				imdbRating: 7.2,
				imdbVotes: 12,
				title: 'The Fairest Fair Lady',
				type: 'MOVIE',
				version: 0,
				plot: null,
				awards: null,
				released: null
			}
		};


		mockBackend.connections.subscribe((connection: MockConnection) => {
			switch (connection.request.method) {
				case RequestMethod.Patch: {
					const response = [
						{
							op: 'POST',
							path: 'Article',
							value: {
								data: {
									type: 'Article',
									id: '1',
									attributes: {
										title: 'Updated Title'
									}
								}
							}
						}
					];

					connection.mockRespond(new Response(new ResponseOptions({
						body: response
					})));
					break;
				}
				default:
					throw new Error(`Unexpected request method: ${connection.request.method}`);
			}
		});
	}

});
