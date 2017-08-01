import {Subject} from "rxjs/Subject";
import {Observable} from "rxjs/Observable";
import * as _ from "lodash";
import "rxjs/add/operator/zip";
import "rxjs/add/operator/do";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/distinct";
import "rxjs/add/operator/switch";
import {FilteringParam, ManyQueryResult, NgrxJsonApiService, Query, ResourceIdentifier} from "ngrx-json-api";
import {NgrxBindingUtils} from "./crnk.binding.utils";

export interface SelectorBindingConfig {
	query: Query;
	filterFactory: (string) => FilteringParam;
}

export class SelectorBinding {

	private querySubject: Subject<string>;
	public values: Observable<Array<ResourceIdentifier>>;

	constructor(private service: NgrxJsonApiService, private config: SelectorBindingConfig, private utils: NgrxBindingUtils) {

		const termToQuery = (queryTerm: String): Query => {
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
		};

		const executeQuery = (query: Query): Observable<ManyQueryResult> => this.service.findMany({
			query: query,
			fromServer: true
		});
		const isNotLoading = (result: ManyQueryResult): boolean => !result.loading;
		const toIdentifiers = (result: ManyQueryResult): Array<ResourceIdentifier> => this.utils.toResourceIdentifiers(result.data);

		this.querySubject = new Subject<string>();
		this.values = this.querySubject.asObservable()
			.map(termToQuery)
			.switchMap(executeQuery)
			.filter(isNotLoading)
			.map(toIdentifiers);
	}

	public complete(event: any) {
		this.querySubject.next(event.query);
	}
}
