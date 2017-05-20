import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {RouterModule} from "@angular/router";
import {AppComponent} from "./app.component";
import {BrowseComponent} from "./browse";
import {
	InputTextModule,
	InputTextareaModule,
	DropdownModule,
	DataTableModule,
	AutoCompleteModule,
	SharedModule,
	DataGridModule,
	DataListModule,
	ButtonModule,
	DataScrollerModule,
	PaginatorModule,
	PanelModule,
	TreeTableModule
} from "primeng/primeng";
import {LocalStorageModule} from "angular-2-local-storage";
import {ROUTES} from "./app.routes";
import {SelectButtonModule} from "primeng/components/selectbutton/selectbutton";
import {ToggleButtonModule} from "primeng/components/togglebutton/togglebutton";
import {BrowseService, BrowseUtils, BrowsePreferencesService} from "./browse/browse.service";


@NgModule({
	declarations: [
		AppComponent,
		BrowseComponent,
	],
	imports: [
		BrowserModule,
		FormsModule,
		HttpModule,
		RouterModule.forRoot(ROUTES, {useHash: true}),

		LocalStorageModule.withConfig({
			prefix: 'my-app',
			storageType: 'localStorage'
		}),

		// primeng
		DropdownModule, SelectButtonModule, ToggleButtonModule,
		InputTextModule, InputTextareaModule, DataTableModule, SharedModule, AutoCompleteModule,
		DataGridModule, DataListModule, DataScrollerModule, DataTableModule,
		DropdownModule, ButtonModule,
		PaginatorModule, PanelModule,
		TreeTableModule

	],
	providers: [
		BrowseService, BrowseUtils, BrowsePreferencesService
	],
	bootstrap: [
		AppComponent
	]
})
export class AppModule {

}
