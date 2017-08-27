import {initialNgrxJsonApiState} from "ngrx-json-api/src/reducers";
import {testPayload} from "./crnk.operations.spec.utils";
import {ActionReducerMap, StoreModule} from "@ngrx/store";
import {updateStoreDataFromPayload} from "ngrx-json-api/src/utils";
import {NgrxJsonApiModule} from "ngrx-json-api";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {EffectsModule} from "@ngrx/effects";
import {HttpClientModule} from "@angular/common/http";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {CrnkBindingModule} from "../../main/typescript/binding/crnk.binding.module";
import {CrnkExpressionModule} from "../../main/typescript/expression/forms/crnk.expression.form.module";
import {CommonModule} from "@angular/common";


export const testReducer: ActionReducerMap<any> = {};

let initialState = {
	NgrxJsonApi: {
		api: {
			...{},
			...initialNgrxJsonApiState,
			...{
				data: updateStoreDataFromPayload({}, testPayload),
			},
		},
	},
};

@NgModule({
	imports: [
		CommonModule,
		StoreModule.forRoot(testReducer, {initialState: initialState}),
		EffectsModule.forRoot([]),
		FormsModule,
		CrnkExpressionModule,
		CrnkBindingModule,
		HttpClientTestingModule,
		HttpClientModule,
		NgrxJsonApiModule.configure({
			resourceDefinitions: [],
			apiUrl: 'myapi.com',
		}),
	],
	exports: [
		CommonModule,
		FormsModule,
		CrnkExpressionModule,
		CrnkBindingModule,
		HttpClientTestingModule,
		HttpClientModule,
		NgrxJsonApiModule
	],
	providers: [],
})
export class TestingModule {
}
