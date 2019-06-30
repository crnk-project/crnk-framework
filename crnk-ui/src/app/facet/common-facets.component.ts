import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatTableDataSource} from '@angular/material';
import {ActivatedRoute} from '@angular/router';
import {EcosystemHealth} from '~/ecosystem.health';
import {BehaviorSubject, Observable} from 'rxjs';
import {Facet, FacetListResult} from '~/facet';
import {environment} from '~/environment';
import {distinctUntilChanged, map, mergeMap, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {FilterSpec} from '~/filter.spec';
import * as _ from 'lodash';

@Component({
	selector: 'sb4b-facets',
	templateUrl: './common-facets.component.html',
	styleUrls: ['common-facets.component.css'],
	exportAs: 'facets'
})
export class CommonFacetsComponent implements OnInit {
	public displayedColumns = ['severity', 'memberId', 'environment', 'component', 'kind', 'code', 'details'];

	public dataSource: MatTableDataSource<EcosystemHealth[]>;

	@Input()
	public path: string;

	@Input()
	public resourceType: string;

	public facets$: Observable<Facet[]>;

	public facetSelections: { [facetName: string]: string[] } = {};

	public facets: Facet[];

	private parametersSubject = new BehaviorSubject<{ [param: string]: string }>(null);

	@Output()
	public filterChange = new EventEmitter<FilterSpec[]>();

	@Output()
	public facetsLoaded = new EventEmitter< Facet[]>();

	constructor(private route: ActivatedRoute, private httpClient: HttpClient) {
	}

	ngOnInit() {
		const url = this.computeUrl(this.path);

		// stream of continuous selections and subsequent HTTP requests
		this.facets$ = this.parametersSubject.asObservable().pipe(
			distinctUntilChanged((params1, params2) => _.isEqual(params1, params2)),
			mergeMap(params => this.httpClient.get<FacetListResult>(url, {params: params})),
			map(response => response.data ? response.data : null),
			tap(it => this.facets = it),
			tap(it => this.facetsLoaded.emit(it))
		);
		//	this.facets$.subscribe(Subscriber.create(it => console.log('sub', it)));

		// trigger first request
		this.updateFacets();
	}

	public get filter(): FilterSpec[] {
		const filters: FilterSpec[] = [];

		for (const facet of this.facets) {
			const selectedLabels = this.facetSelections[facet.name];
			if (selectedLabels && selectedLabels.length > 0) {
				const facetFilters = selectedLabels.map(label => facet.values[label].filterSpec);
				if (facetFilters.length === 1) {
					filters.push(...facetFilters);
					// tslint:disable-next-line:brace-style
				} else {
					filters.push({
						operator: 'OR',
						expression: facetFilters
					});
				}
			}
		}
		return filters;
	}

	updateFacets() {
		const params = {
			// 'filter[values][GROUP]': 'memberId,environment',
			'filter[resourceType]': this.resourceType
		};

		// add facet selections
		for (const facetName of _.sortBy(_.keys(this.facetSelections))) {
			const selectedLabels = this.facetSelections[facetName];
			if (selectedLabels && selectedLabels.length > 0) {
				params[`filter[values.${facetName}][SELECT]`] = selectedLabels.join(',');
			}
		}

		this.parametersSubject.next(params);
	}

	updateFilter(facet, selectedLabels) {
		if (!_.isEqual(this.facetSelections[facet.name], selectedLabels)) {
			this.facetSelections[facet.name] = selectedLabels;

			const facetIndex = this.facets.indexOf(facet);
			if (facetIndex === -1) {
				throw new Error('unable to find facet');
			}

			for (let i = facetIndex + 1; i < this.facets.length; i++) {
				const otherFacet = this.facets[i];
				this.facetSelections[otherFacet.name] = [];
			}

			this.updateFacets();

			this.filterChange.emit(this.filter);
		}
	}

	private computeUrl(contextPath: string) {
		let path = contextPath;
		if (path.startsWith('/api/')) {
			path = path.substring(5);
		}
		return environment.apiUrl + '/' + path + '/facet';
	}

}

