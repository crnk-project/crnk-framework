import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {FacetListResult} from '~/facet';
import {environment} from '~/environment';
import {map} from 'rxjs/operators';
import {BackendService} from '~/service/backend.service';
import {Observable} from 'rxjs';
import {UiExplorer} from '~/ui.explorer';


@Component({
	selector: 'sb4b-data-explorer',
	templateUrl: './data-browser-explorer.component.html',
	styleUrls: ['data-browser-explorer.component.css']
})
export class DataBrowserExplorerComponent implements OnInit{
	public displayedColumns = [];

	public sort = [];

	private explorer$: Observable<UiExplorer>;

	constructor(private route: ActivatedRoute, private backend: BackendService) {
		const explorerId = this.route.snapshot.params.explorer;

		this.explorer$ = this.backend.getExplorer(explorerId);

	}

	ngOnInit(): void {


	}
}

