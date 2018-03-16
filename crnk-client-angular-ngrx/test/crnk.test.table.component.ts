import { Component, OnInit } from '@angular/core';
import { MetaAttributeListResult } from '../meta/meta.attribute';
import { CrnkBindingService } from '../binding/crnk.binding.service';
import { DataTableBinding } from '../binding/crnk.binding.table';
import { DataTableBindingConfig } from '../binding';
import { Observable } from 'rxjs/Observable';

@Component({
	selector: 'test-table',
	templateUrl: 'crnk.test.table.component.html'
})
export class TestTableComponent implements OnInit {

	public binding: DataTableBinding;

	public result: Observable<MetaAttributeListResult>;

	public config: DataTableBindingConfig = {
		queryId: 'tableQuery',
		fromServer: false
	}

	constructor(private bindingService: CrnkBindingService) {
	}

	ngOnInit() {
		this.binding = this.bindingService.bindDataTable(this.config);
		this.result = this.binding.result$.map(
			it => it as MetaAttributeListResult
		);
	}
}