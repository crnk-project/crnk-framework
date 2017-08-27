import {Injector, NgModule} from '@angular/core';
import 'rxjs/add/operator/zip';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinct';
import 'rxjs/add/operator/switch';
import {CrnkBindingService} from './crnk.binding.service';
import {OperationsService} from '../operations';
import {NgrxJsonApiModule, NgrxJsonApiService} from 'ngrx-json-api';
import {CrnkBindingUtils} from './crnk.binding.utils';
import {ControlErrorsComponent, ResourceErrorsComponent} from "./crnk.binding.error";
import {CommonModule} from "@angular/common";
import {CrnkExpressionModule} from "../expression/forms/crnk.expression.form.module";
import {Store} from "@ngrx/store";
import {NgrxJsonApiSelectors} from "ngrx-json-api/src/selectors";

export function bingingServiceFactory(service: NgrxJsonApiService, utils: CrnkBindingUtils, store: Store<any>,
									  injector: Injector) {
	const operationsService = injector.get(OperationsService, null);
	const selectors = injector.get(NgrxJsonApiSelectors, null);

	return new CrnkBindingService(service, utils, store, selectors, operationsService);
}


@NgModule({
	imports: [
		CommonModule,
		NgrxJsonApiModule,
		CrnkExpressionModule
	],
	exports: [
		CommonModule,
		NgrxJsonApiModule,
		CrnkExpressionModule,

		ControlErrorsComponent, ResourceErrorsComponent
	],
	declarations: [
		ControlErrorsComponent, ResourceErrorsComponent
	],
	providers: [
		{
			provide: CrnkBindingService,
			useFactory: bingingServiceFactory,
			deps: [NgrxJsonApiService, CrnkBindingUtils, Store, Injector]
		},
		{
			provide: CrnkBindingUtils,
			useClass: CrnkBindingUtils
		}
	]
})
export class CrnkBindingModule {

}
