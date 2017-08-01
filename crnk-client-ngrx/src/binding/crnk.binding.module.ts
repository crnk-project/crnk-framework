import {NgModule, ModuleWithProviders, Injector} from '@angular/core';
import 'rxjs/add/operator/zip';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinct';
import 'rxjs/add/operator/switch';
import {JsonApiBindingService} from './crnk.binding.service';
import {OperationsService} from '../operations';
import {NgrxJsonApiModule, NgrxJsonApiService} from 'ngrx-json-api';
import {NgrxBindingUtils} from './crnk.binding.utils';

export function bingingServiceFactory(service: NgrxJsonApiService, utils: NgrxBindingUtils, injector: Injector) {
	const operationsService = injector.get(OperationsService, null);

	return new JsonApiBindingService(service, utils, operationsService);
}


@NgModule({
	imports: [
		NgrxJsonApiModule
	],
	exports: [
		NgrxJsonApiModule
	],
	declarations: []
})
export class ArbJsonApiBindingModule {

	static configure(): ModuleWithProviders {
		return {
			ngModule: NgrxJsonApiModule,
			providers: [
				{
					provide: JsonApiBindingService,
					useFactory: bingingServiceFactory,
					deps: [NgrxJsonApiService, NgrxBindingUtils, Injector]
				},
				{
					provide: NgrxBindingUtils,
					useClass: NgrxBindingUtils
				}
			]
		};
	}

}
