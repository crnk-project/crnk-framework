import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCheckboxModule, MatListModule, MatTableModule} from '@angular/material';
import {CommonBindingComponent} from './common-binding.component';
import {FormsModule} from '@angular/forms';


@NgModule({
	imports: [
		CommonModule,
		MatTableModule,
		MatCheckboxModule,
		MatListModule,
		FormsModule
	],
	declarations: [
		CommonBindingComponent
	],
	exports: [
		CommonBindingComponent,
	],
})
export class CommonBindingModule {

}
