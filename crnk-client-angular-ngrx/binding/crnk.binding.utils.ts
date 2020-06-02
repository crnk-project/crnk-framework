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

export const applyQueryParams = function (baseQuery: Query, additionalParams: QueryParams) {
	if (!baseQuery.params) {
		baseQuery.params = {};
	}

	baseQuery.params.limit = additionalParams.limit;
	baseQuery.params.offset = additionalParams.offset;
	if (!_.isEmpty(additionalParams.include)) {
		// combine inclusions
		if (!baseQuery.params.include) {
			baseQuery.params.include = [];
		}
		baseQuery.params.include = _.union(baseQuery.params.include, additionalParams.include);
	}
	if (!_.isEmpty(additionalParams.fields)) {
		// combine fields
		if (!baseQuery.params.fields) {
			baseQuery.params.fields = [];
		}
		baseQuery.params.fields = _.union(baseQuery.params.fields, additionalParams.fields);
	}
	if (additionalParams.sorting != null) {
		// replace sort (additionalParams.sorting != null check to allow to clear sorting)
		// unlikely to be a use case to merge sorting params
		if (!baseQuery.params.sorting) {
			baseQuery.params.sorting = [];
		}
		baseQuery.params.sorting = [...additionalParams.sorting];
	}
	if (!_.isEmpty(additionalParams.filtering)) {
		// combine filters by replacing duplicates on same attribute
		if (!baseQuery.params.filtering) {
			baseQuery.params.filtering = [];
		}

		const filterMap = _.keyBy(baseQuery.params.filtering, 'path');
		const addFilterMap = _.keyBy(additionalParams.filtering, 'path');
		Object.assign(filterMap, addFilterMap);
		baseQuery.params.filtering = _.values(filterMap);
	}
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

	public applyQueryParams(baseQuery: Query, additionalParams: QueryParams) {
		return applyQueryParams(baseQuery, additionalParams);
	}


	public toSearchPath(attributePath: string | Expression<any>) {
		return toQueryPath(attributePath);
	}

}
