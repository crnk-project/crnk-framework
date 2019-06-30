import {Component, Directive, ElementRef, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatTableDataSource} from '@angular/material';
import {ActivatedRoute} from '@angular/router';
import {BehaviorSubject, Observable, Subscriber} from 'rxjs';
import {environment} from '~/environment';
import {distinctUntilChanged, map, mergeMap, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {FilterSpec} from '~/filter.spec';
import {Facet, FacetListResult} from '~/facet';
import * as _ from 'lodash';

/*
@Directive({
	selector: '[dataSource]'
})
class DataSourceDirective {

	@Input() tooltip;
	private _tooltipEle;

	constructor(private el: CommonBindingComponent) {}


}
*/

@Component({
	selector: 'sb4b-binding',
	templateUrl: './common-binding.component.html',
	styleUrls: ['common-binding.component.css'],
	exportAs: 'binding'
})
export class CommonBindingComponent implements OnInit {

	public dataSource$: Observable<MatTableDataSource<any>>;

	@Input()
	public path: string;

	@Input()
	public resourceType: string;

	@Input()
	public sort: string[];

	public _filters: FilterSpec[] = [];

	private _filterChanges: EventEmitter<FilterSpec[]>;

	private filterSubject = new BehaviorSubject<FilterSpec[]>([]);

	private filterSubscriber = Subscriber.create((filters: FilterSpec[]) => this.filterSubject.next(filters));

	constructor(private route: ActivatedRoute, private httpClient: HttpClient) {
	}

	@Input()
	public set filterChanges(filterChanges: EventEmitter<FilterSpec[]>) {
		this._filterChanges = filterChanges;
		if (filterChanges) {
			filterChanges.subscribe(this.filterSubscriber);
		}
	}

	public get filterChanges() {
		return this._filterChanges;
	}


	@Input()
	public set filters(filters: FilterSpec[]) {
		this._filters = filters;
	}

	public get filters() {
		return this._filters;
	}

	private mapToParameters(filters: FilterSpec[], sort: string[]) {
		const params = {};
		if (sort) {
			params['sort'] = sort;
		}
		if (filters) {
			params['filter'] = JSON.stringify(filters);
		}
		return params;
	}

	ngOnInit() {
		const url = this.computeUrl(this.path);

		// stream of continuous UI inputs and subsequent HTTP requests
		this.dataSource$ = this.filterSubject.pipe(
			map(filters => this.mapToParameters(filters, this.sort)),
			distinctUntilChanged((params1, params2) => _.isEqual(params1, params2)),
			mergeMap(params => this.httpClient.get<FacetListResult>(url, {params: params})),
			map(response => response.data ? response.data : null),
			map(data => new MatTableDataSource(data)),
		);
	}

	private computeUrl(contextPath: string) {
		let path = contextPath;
		if (path.startsWith('/api/')) {
			path = path.substring(5);
		}
		return environment.apiUrl + '/' + path;
	}

}

