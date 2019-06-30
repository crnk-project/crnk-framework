import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {BackendService} from '~/service/backend.service';
import {UiExplorer} from '~/ui.explorer';
import {TableColumnElement} from '~/table.column.element';
import * as _ from 'lodash';

@Component({
	selector: 'sb4b-data-explorer-table',
	templateUrl: './data-browser-explorer-table.component.html',
	styleUrls: ['data-browser-explorer-table.component.css']
})
export class DataBrowserExplorerTableComponent implements OnInit {
	public displayedColumns = ['id'];

	public sort = [];

	@Input()
	public explorer: UiExplorer;

	private columns: TableColumnElement[];

	private accessors: { [columnName: string]: any };

	public facetsHidden: true;

	constructor(private route: ActivatedRoute, private backend: BackendService) {
	}

	ngOnInit(): void {
		this.displayedColumns = this.explorer.table.columns.elementIds;
		this.columns = this.displayedColumns.map(id => this.explorer.table.columns.elements[id]);
		console.log('columns', this.displayedColumns, this.columns);

		this.accessors = {};
		for (let column of this.columns) {
			const path = column.attributePath.split('.');
			if (path.length == 1) {
				this.accessors[column.id] = (resource) => resource[column.attributePath];
			} else {
				this.accessors[column.id] = (resource) => _.get(resource, column.attributePath);
			}
		}
	}


	public getValue(resource: any, column: TableColumnElement) {
		return this.accessors[column.id](resource);
	}
}

