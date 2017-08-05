import {Injectable, Optional} from '@angular/core';
import 'rxjs/add/operator/zip';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinct';
import 'rxjs/add/operator/switch';
import {DataTableBindingConfig, DataTableBinding} from './crnk.binding.table';
import {FormBindingConfig, FormBinding} from './crnk.binding.form';
import {SelectorBindingConfig, SelectorBinding} from './crnk.binding.selector';
import {OperationsService} from '../operations';
import {NgrxJsonApiService} from 'ngrx-json-api';
import {NgrxBindingUtils} from './crnk.binding.utils';

@Injectable()
export class JsonApiBindingService {

	constructor(private ngrxJsonApiService: NgrxJsonApiService, private utils: NgrxBindingUtils,
		@Optional() private operationsService: OperationsService) {

	}

	public bindDataTable(config: DataTableBindingConfig): DataTableBinding {
		return new DataTableBinding(this.ngrxJsonApiService, config, this.utils);
	}

	public bindForm(config: FormBindingConfig): FormBinding {
		return new FormBinding(this.ngrxJsonApiService, config, this.operationsService);
	}

	public bindSelector(config: SelectorBindingConfig): SelectorBinding {
		return new SelectorBinding(this.ngrxJsonApiService, config, this.utils);
	}
}
