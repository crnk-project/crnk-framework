import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule, MatTableModule, MatTabsModule} from '@angular/material';
import {PublicGuard} from 'ngx-auth';
import {DataBrowserOverviewComponent} from './data-browser-overview.component';
import {CommonFacetsModule} from 'app/common/facet/index';
import {CommonBindingModule} from 'app/common/binding/index';
import {DataBrowserExplorerComponent} from '~/data-browser/data-browser-explorer.component';
import {DataBrowserExplorerTableComponent} from '~/data-browser/data-browser-explorer-table.component';

export const ECOSYSTEM_HEALTH: Routes = [
	{
		path: 'data-browser',
		canActivate: [PublicGuard],
		component: DataBrowserOverviewComponent
	},
	{
		path: 'data-browser/:explorer',
		canActivate: [PublicGuard],
		component: DataBrowserExplorerComponent
	}
];

@NgModule({
	imports: [
		CommonModule,
		CommonFacetsModule,
		CommonBindingModule,
		MatTabsModule,
		MatButtonModule,
        MatTableModule,
		RouterModule.forChild(ECOSYSTEM_HEALTH),
	],
	declarations: [
		DataBrowserOverviewComponent,
		DataBrowserExplorerComponent,
		DataBrowserExplorerTableComponent,
	],
	exports: [
		DataBrowserOverviewComponent,
		DataBrowserExplorerComponent,
		DataBrowserExplorerTableComponent,
	],
})
export class DataBrowserModule {

}
