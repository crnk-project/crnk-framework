import {Response, ResponseOptions} from '@angular/http';

import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/throw';

import {NgrxJsonApi, NgrxJsonApiEffects} from 'ngrx-json-api';

export class JsonApiMock {
	constructor() {
	}

	create(query, document) {
		if (document.data.type === 'SUCCESS') {
			return Observable.of('SUCCESS');
		} else if (document.data.type === 'FAIL') {
			return Observable.throw('FAIL');
		}
	}

	update(query, document) {
		if (document.data.type === 'SUCCESS') {
			return Observable.of('SUCCESS');
		} else if (document.data.type === 'FAIL') {
			return Observable.throw('FAIL');
		}
	}

	find(query) {
		if (query.type === 'SUCCESS') {
			let res = {
				data: {
					type: 'SUCCESS'
				}
			};
			return Observable.of(new Response(
				new ResponseOptions({
					body: JSON.stringify(res),
					status: 200
				})
			));
		} else if (query.type === 'FAIL') {
			return Observable.throw('FAIL QUERY');
		}
	}

	delete(query) {
		if (query.type === 'SUCCESS') {
			return Observable.of(new Response(
				new ResponseOptions({})));
		} else if (query.type === 'FAIL') {
			return Observable.throw('FAIL QUERY');
		}
	}
}

export const MOCK_JSON_API_PROVIDERS = [
	{provide: JsonApiMock, useClass: JsonApiMock},
	{provide: NgrxJsonApi, useExisting: JsonApiMock}
];

/*
export class NgrxJsonApiMockEffects extends NgrxJsonApiEffects {
	// constructor() {
	//   // super()
	// }

	private toErrorPayload(query, response) {
		if (response === 'FAIL QUERY') {
			return {query: query};
		} else if (response === 'Unknown query') {
			return query;
		}
		return ({
			query: query,
			jsonApiData: {data: {type: response}}
		});
	}

	private generatePayload(resource, operation) {
		return resource;
	}
}

export const MOCK_NGRX_EFFECTS_PROVIDERS = [
	{provide: NgrxJsonApiMockEffects, useClass: NgrxJsonApiMockEffects},
	{provide: NgrxJsonApiEffects, useExisting: NgrxJsonApiMockEffects}
];
*/