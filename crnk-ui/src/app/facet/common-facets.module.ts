import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCheckboxModule, MatListModule, MatTableModule} from '@angular/material';
import {CommonFacetsComponent} from './common-facets.component';
import {CommonFacetComponent} from '~/common/facet/common-facet.component';
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
		CommonFacetsComponent,
		CommonFacetComponent
	],
	exports: [
		CommonFacetsComponent,
		CommonFacetComponent,
	],
})
export class CommonFacetsModule {

}
