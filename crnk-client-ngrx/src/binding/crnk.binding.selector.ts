import {Subject} from "rxjs/Subject";
import {Observable} from "rxjs/Observable";
import * as _ from "lodash";
import "rxjs/add/operator/zip";
import "rxjs/add/operator/do";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/distinct";
import "rxjs/add/operator/switch";
import {NgrxJsonApiService, Resource, FilteringParam, Query} from "ngrx-json-api";
import {NgrxBindingUtils} from "./crnk.binding.utils";

export interface SelectorBindingConfig {
	query: Query;
	filterFactory: (string) => FilteringParam;
}

export class SelectorBinding {

	private querySubject = new Subject<string>();
	public values: Observable<Array<Resource>>;

	constructor(private service: NgrxJsonApiService, private config: SelectorBindingConfig, private utils: NgrxBindingUtils) {
		this.values = this.querySubject.map(queryTerm => {
			const query = _.cloneDeep(this.config.query);
			if (queryTerm !== null) {
				if (!query.params) {
					query.params = {};
				}
				if (!query.params.filtering) {
					query.params.filtering = [];
				}

				const filterParam = this.config.filterFactory(queryTerm);
				query.params.filtering.push(filterParam);
			}
			return query;
		})
			.switchMap(query => this.service.findMany({query: query, fromServer: true}))
			.filter(result => !result.loading)
			.map(result => this.utils.toResourceIdentifiers(result.data));
	}

	public complete(event: any) {
		this.querySubject.next(event.query);
	}
}
