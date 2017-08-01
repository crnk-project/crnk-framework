import {NgModule} from "@angular/core";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";

import {HttpModule} from "@angular/http";
import {NgrxJsonApiModule} from "ngrx-json-api";
import {OperationsEffects} from "./crnk.operations.effects";
import {OperationsService} from "./crnk.operations.service";


@NgModule({

	imports: [
		HttpModule,
		StoreModule,
		NgrxJsonApiModule,
		EffectsModule.run(OperationsEffects)
	],
	providers: [
		OperationsService
	]
})
export class ArbOperationsModule {

}
;
