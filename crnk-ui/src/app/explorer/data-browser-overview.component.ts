import {Component} from '@angular/core';
import {ActivatedRoute} from '@angular/router';


@Component({
	selector: 'sb4b-data-overview',
	templateUrl: './data-browser-overview.component.html',
	styleUrls: ['data-browser-overview.component.css']
})
export class DataBrowserOverviewComponent {
	public displayedColumns = ['serviceName', 'resourceType', 'actions'];

	public sort = ['serviceName', 'baseQuery.resourceType'];

	constructor(private route: ActivatedRoute) {
	}
}

