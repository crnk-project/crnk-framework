import {NgModule} from '@angular/core';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';

import {HttpModule} from '@angular/http';
import {NgrxJsonApiModule} from 'ngrx-json-api';
import {OperationsEffects} from './crnk.operations.effects';


@NgModule({
	imports: [
		HttpModule,
		StoreModule,
		NgrxJsonApiModule,
		EffectsModule.forFeature([OperationsEffects])
	]
})
export class CrnkOperationsModule {

}
;
