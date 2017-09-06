import {Component, OnDestroy, OnInit} from '@angular/core';
import {MetaAttributeListResult} from '../meta/meta.attribute';
import {Subscription} from 'rxjs/Subscription';
import {CrnkBindingService} from '../binding/crnk.binding.service';
import {DataTableBinding} from '../binding/crnk.binding.table';

@Component({
	selector: 'test-table',
	templateUrl: 'crnk.test.table.component.html'
})
export class TestTableComponent implements OnInit, OnDestroy {

	binding: DataTableBinding;

	private subscription: Subscription;

	public result: MetaAttributeListResult;

	constructor(private bindingService: CrnkBindingService) {
	}

	ngOnInit() {
		this.binding = this.bindingService.bindDataTable({
			queryId: 'tableQuery',
			fromServer: false
		});
		this.subscription = this.binding.result$.subscribe(
			it => this.result = it as MetaAttributeListResult
		);
	}

	ngOnDestroy() {
		this.subscription.unsubscribe();
	}
}
