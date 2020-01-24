import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import * as _ from 'lodash';
import 'rxjs/add/operator/zip';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinct';
import 'rxjs/add/operator/switch';
import {
	Direction,
	FilteringParam,
	ManyQueryResult,
	NgrxJsonApiService,
	Query,
	Resource,
	SortingParam,
	NgrxJsonApiZoneService, NGRX_JSON_API_DEFAULT_ZONE, getNgrxJsonApiZone, selectNgrxJsonApiZone, QueryParams
} from 'ngrx-json-api';
import { CrnkBindingUtils, toQueryPath } from './crnk.binding.utils';
// TODO get rid of this? or support multiple ones, only dependency to primeng here...
import { LazyLoadEvent } from 'primeng/primeng';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/Subscription';

export interface DataTableImplementationContext {

	/**
	 * applies the given query parameters
	 *
	 * @param {QueryParams} param
	 */
	applyQueryParams(param: QueryParams);

}

export interface DataTableImplementationAdapter {

	/**
	 * Called on startup and provides access to interface with binding implementation.
	 *
	 * @param {DataTableImplementationContext} context
	 */
	init(context: DataTableImplementationContext);

	/**
	 * @param event called by table implementation to update the query
	 */
	onLazyLoad(event);
}

export interface DataTableBindingConfig {

	/**
	 * Query to use from the store to specify sort, filter, page parameters and get results.
	 */
	queryId: string;

	/**
	 * Query to use as base query. Table filter will be applied on top of this query.
	 * If not specified, the query from the store will be taken based on the specified
	 * queryId.
	 */
	baseQuery?: Query;

	fromServer?: boolean;

	/**
	 * Zone to use within ngrx-json-api.
	 */
	zoneId?: string;

	/**
	 * Table implementation to use, currently the PrimeNG DataTable is supported out-of-the-box.
	 */
	implementationAdapter?: DataTableImplementationAdapter;

	/**
	 * Additional query parameters (sorting, filtering, etc.) that will be applied to the query.
	 * As long as the observable emits no value, it is ignored. No waiting takes place.
	 */
	customQueryParams?: Observable<QueryParams>;
}

export class DataTableBinding {

	public result$: Observable<ManyQueryResult>;

	private baseQuery: Query = null;

	private latestQuery: Query = null;

	private latestImplementationQueryParams: QueryParams = null;

	private latestExternalQueryParams: QueryParams = null;

	public implementationAdapter: DataTableImplementationAdapter;

	private customQueryParamsSubscription: Subscription;

	private zoneId: string;

	public readonly zone: NgrxJsonApiZoneService;

	constructor(ngrxJsonApiService: NgrxJsonApiService, private config: DataTableBindingConfig,
		private utils: CrnkBindingUtils, private store: Store<any>
	) {

		this.zoneId = config.zoneId || NGRX_JSON_API_DEFAULT_ZONE;
		this.zone = ngrxJsonApiService.getZone(this.zoneId);

		this.implementationAdapter = this.config.implementationAdapter;
		if (!this.implementationAdapter) {
			this.implementationAdapter = new DataTablePrimengAdapter();
		}
		this.implementationAdapter.init({
			applyQueryParams: implementationParams => this.setImplementationQueryParams(implementationParams)
		});

		if (!this.config.queryId) {
			throw new Error('no queryId specified in config');
		}
		if (_.isUndefined(this.config.fromServer)) {
			this.config.fromServer = true;
		}

		this.initBaseQuery();

		this.result$ = this.zone
			.selectManyResults(this.config.queryId, true)
			.do(this.setBaseQueryIfNecessary)
			.map(this.guardAgainstEmptyQuery)
			.finally(() => this.cancelSubscriptions)
			.share();
	}


	private checkSubscriptions() {
		if (!this.customQueryParamsSubscription && this.config.customQueryParams) {
			this.customQueryParamsSubscription = this.config.customQueryParams.subscribe(customQueryParams => {
				this.latestExternalQueryParams = customQueryParams;
				this.updateQueryParams();
			});
		}
	}

	private cancelSubscriptions() {
		if (this.customQueryParamsSubscription !== null) {
			this.customQueryParamsSubscription.unsubscribe();
			this.customQueryParamsSubscription = null;
		}
	}

	private initBaseQuery() {
		this.baseQuery = this.config.baseQuery;
		if (!this.baseQuery) {
			this.store.let(selectNgrxJsonApiZone(this.zoneId)).take(1)
				.map(it => it.queries[this.config.queryId])
				.subscribe(storeQuery => {
					if (storeQuery != null) {
						this.latestQuery = storeQuery.query;
						this.baseQuery = storeQuery.query;
					}
				});
		}
		if (!this.baseQuery) {
			throw new Error('baseQuery not available');
		}
	}

	private setBaseQueryIfNecessary(result: ManyQueryResult) {
		if (this.baseQuery === null) {
			this.baseQuery = result.query;
			this.latestQuery = result.query;
		}
	}

	private guardAgainstEmptyQuery(result: ManyQueryResult): ManyQueryResult {
		// query not available until load event trigger by PrimeNG
		// return empty object in that case
		if (result) {
			return result;
		}
		else {
			const emptyResult: ManyQueryResult = {
				query: null,
				loading: false,
				resultIds: [],
				meta: {},
				links: {},
				data: [],
				errors: []
			};
			return emptyResult;
		}
	}

	public refresh() {
		this.zone.refreshQuery(this.config.queryId);
	}

	private setImplementationQueryParams(implementationParams: QueryParams) {
		this.checkSubscriptions();
		this.latestImplementationQueryParams = implementationParams;
		this.updateQueryParams();
	}

	private updateQueryParams() {
		if (!this.latestImplementationQueryParams) {
			// implementation not ready yet, wait
			// note that custom parameters are optional and no waiting takes palce
			return;
		}
		if (!this.baseQuery) {
			throw new Error('illegal state, base query not available');
		}

		const query = _.cloneDeep(this.baseQuery);

		if (this.latestExternalQueryParams) {
			this.utils.applyQueryParams(query, this.latestExternalQueryParams);
		}
		this.utils.applyQueryParams(query, this.latestImplementationQueryParams);

		if (!_.isEqual(query, this.latestQuery)) {
			this.zone.putQuery({
					query: query,
					fromServer: this.config.fromServer
				}
			);
			this.latestQuery = query;
		}

	}

	public onLazyLoad(event) {
		this.implementationAdapter.onLazyLoad(event);
	}
}

/**
 * Maps an filter input value to a JSON API filter value.
 */
export interface DataFilterValueMapper {

	mapFilterValue(attributePath: string, operator: string, value);
}

export class DefaultDataFilterValueMapper implements DataFilterValueMapper {

	mapFilterValue(attributePath: string, operator: string, value) {
		if (operator === 'like') {
			return '%' + value + '%';
		}
		return value;
	}
}

export class DataTablePrimengAdapter implements DataTableImplementationAdapter {

	private context: DataTableImplementationContext;

	public defaultMatchMode = 'like';

	public filterValueMapper: DataFilterValueMapper = new DefaultDataFilterValueMapper();

	public init(context: DataTableImplementationContext) {
		this.context = context;
	}

	public onLazyLoad(event: LazyLoadEvent) {
		const offset = event.first;
		const limit = event.rows;
		const filters: Array<FilteringParam> = [];
		const sorting: Array<SortingParam> = [];
		const includes: Array<string> = [];

		if (event.multiSortMeta) {
			for (const sortMeta of event.multiSortMeta) {
				const direction = sortMeta.order === -1 ? Direction.DESC : Direction.ASC;
				sorting.push({ direction: direction, api: toQueryPath(sortMeta.field) });
			}
		}
		else if (event.sortField) {
			const direction = event.sortOrder === -1 ? Direction.DESC : Direction.ASC;
			sorting.push({ direction: direction, api: toQueryPath(event.sortField) });
		}
		if (event.filters) {
			for (const filterKey of Object.keys(event.filters)) {
				const attributePath = toQueryPath(filterKey);
				const filterMeta = event.filters[filterKey];
				let matchMode = filterMeta.matchMode;
				const value = filterMeta.value;
				if (!matchMode) {
					matchMode = this.defaultMatchMode;
				}
				const mappedValue = this.filterValueMapper.mapFilterValue(attributePath, matchMode, value);
				filters.push({ value: mappedValue, path: attributePath, operator: matchMode });
			}
		}

		this.context.applyQueryParams({
			limit: limit,
			offset: offset,
			include: includes,
			sorting: sorting,
			filtering: filters
		});
	}
}
