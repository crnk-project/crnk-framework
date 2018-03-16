import { Injectable } from '@angular/core';
import 'rxjs/add/operator/zip';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinct';
import 'rxjs/add/operator/switch';
import 'rxjs/add/operator/filter';
import {
	NgrxJsonApiStore,
	NgrxJsonApiStoreData,
	Query,
	QueryParams,
	QueryResult,
	ResourceError,
	ResourceIdentifier,
	StoreResource
} from 'ngrx-json-api';

import * as _ from 'lodash';
import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';
import { Expression } from '../expression';


export const getNgrxJsonApiStore$ = function (state$: Store<any>): Observable<NgrxJsonApiStore> {
	return state$.select('NgrxJsonApi').map(it => it ? it['api'] : undefined) as  Observable<NgrxJsonApiStore>;
};

export const getStoreData$ = function (state$: Store<NgrxJsonApiStore>): Observable<NgrxJsonApiStoreData> {
	return state$.select('data') as  Observable<NgrxJsonApiStoreData>;
};

export const waitWhileLoading = function () {
	return (result$: Observable<QueryResult>) => {
		return result$.filter(it => !it.loading);
	};
};

export const assumeNoError = function () {
	return (result$: Observable<QueryResult>) => {
		return result$.do(it => {
			if (!_.isEmpty(it.errors)) {
				throw new QueryError(it.errors);
			}
		});
	};
};

export const applyQueryParams = function (query: Query, params: QueryParams) {
	if (!query.params) {
		query.params = {};
	}

	query.params.limit = params.limit;
	query.params.offset = params.offset;
	if (!_.isEmpty(params.include)) {
		if (!query.params.include) {
			query.params.include = [];
		}
		query.params.include.push(...params.include);
	}
	if (!_.isEmpty(params.fields)) {
		if (!query.params.fields) {
			query.params.fields = [];
		}
		query.params.fields.push(...params.fields);
	}
	if (!_.isEmpty(params.sorting)) {
		if (!query.params.sorting) {
			query.params.sorting = [];
		}
		query.params.sorting.push(...params.sorting);
	}
	if (!_.isEmpty(params.filtering)) {
		if (!query.params.filtering) {
			query.params.filtering = [];
		}
		query.params.filtering.push(...params.filtering);
	}
};

export const toQueryPath = function (attributePath: string | Expression<any>): string {
	const strAttributePath = attributePath.toString();

	const pathElements = strAttributePath.split('.');

	const searchPath = [];

	for (let i = 0; i < pathElements.length; i++) {
		if (pathElements[i] === 'attributes') {
			// nothing to do
		}
		else if (pathElements[i] === 'relationships' && i < pathElements.length - 2) {
			const relationshipName = pathElements[i + 1];
			const dataType = pathElements[i + 2];
			if (dataType === 'data' || dataType === 'reference') {
				searchPath.push(relationshipName);
				i += 2;
			}
			else {
				throw new Error('cannot map relationship path in ' + attributePath + ', got ' + dataType +
					' but expected data or reference');
			}
		}
		else {
			searchPath.push(pathElements[i]);
		}
	}
	return _.join(searchPath, '.');
};

export class QueryError extends Error {

	constructor(public errors: Array<ResourceError>) {
		super();
	}
}

@Injectable()
export class CrnkBindingUtils {


	public toResourceIdentifier(resource: StoreResource): ResourceIdentifier {
		return { type: resource.type, id: resource.id };
	};

	public toResourceIdentifiers(resources: Array<StoreResource>): Array<ResourceIdentifier> {
		const results: Array<ResourceIdentifier> = [];
		for (const resource of resources) {
			results.push(this.toResourceIdentifier(resource));
		}
		return results;
	}

	public applyQueryParams(query: Query, params: QueryParams) {
		return applyQueryParams(query, params);
	}


	public toSearchPath(attributePath: string | Expression<any>) {
		return toQueryPath(attributePath);
	}

}
