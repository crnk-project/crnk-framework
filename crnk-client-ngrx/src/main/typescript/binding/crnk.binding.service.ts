import {Injectable, Optional} from '@angular/core';
import 'rxjs/add/operator/zip';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinct';
import 'rxjs/add/operator/switch';
import {DataTableBinding, DataTableBindingConfig} from './crnk.binding.table';
import {FormBinding, FormBindingConfig} from './crnk.binding.form';
import {SelectorBinding, SelectorBindingConfig} from './crnk.binding.selector';
import {OperationsService} from '../operations';
import {NgrxJsonApiService} from 'ngrx-json-api';
import {CrnkBindingUtils} from './crnk.binding.utils';
import {Store} from "@ngrx/store";
import {NgrxJsonApiSelectors} from "ngrx-json-api/src/selectors";

@Injectable()
export class CrnkBindingService {

	constructor(private ngrxJsonApiService: NgrxJsonApiService, private utils: CrnkBindingUtils,
				private store: Store<any>,
				private ngrxJsonApiSelectors: NgrxJsonApiSelectors<any>,
				@Optional() private operationsService: OperationsService) {

	}

	public bindDataTable(config: DataTableBindingConfig): DataTableBinding {
		return new DataTableBinding(this.ngrxJsonApiService, config, this.utils);
	}

	public bindForm(config: FormBindingConfig): FormBinding {
		return new FormBinding(this.ngrxJsonApiService, config, this.operationsService, this.store, this.ngrxJsonApiSelectors);
	}

	public bindSelector(config: SelectorBindingConfig): SelectorBinding {
		return new SelectorBinding(this.ngrxJsonApiService, config, this.utils);
	}
}
