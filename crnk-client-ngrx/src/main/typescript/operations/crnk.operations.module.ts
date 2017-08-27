import {NgModule} from "@angular/core";
import {ActionReducerMap, StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";

import {HttpModule} from "@angular/http";
import {NgrxJsonApiModule} from "ngrx-json-api";
import {OperationsEffects} from "./crnk.operations.effects";
import {OperationsService} from "./crnk.operations.service";
import {OperationsStoreReducer} from "./crnk.operations.reducer";


export const operationsReducer: ActionReducerMap<any> = {
	api: OperationsStoreReducer,
};


@NgModule({

	imports: [
		HttpModule,
		StoreModule,
		NgrxJsonApiModule,
		EffectsModule.forFeature([OperationsEffects]),
		StoreModule.forFeature('NgrxJsonApi', operationsReducer, {}),
	],
	providers: [
		OperationsService
	]
})
export class CrnkOperationsModule {

}
;
