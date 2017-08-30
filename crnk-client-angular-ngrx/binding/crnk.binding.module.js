"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
		var c = arguments.length, r = c < 3 ? target :
			desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
		if (typeof Reflect === "object" && typeof Reflect.decorate === "function") {
			r =
				Reflect.decorate(decorators, target, key, desc);
		}
		else {
			for (var i = decorators.length - 1; i >= 0; i--) {
				if (d = decorators[i]) {
					r =
						(c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
				}
			}
		}
		return c > 3 && r && Object.defineProperty(target, key, r), r;
	};
var core_1 = require('@angular/core');
require('rxjs/add/operator/zip');
require('rxjs/add/operator/do');
require('rxjs/add/operator/debounceTime');
require('rxjs/add/operator/distinct');
require('rxjs/add/operator/switch');
var primeng_binding_table_1 = require('./primeng.binding.table');
var primeng_binding_form_1 = require('./primeng.binding.forFormElement');
var primeng_binding_selector_1 = require('./primeng.binding.selector');
var primeng_binding_service_1 = require('./primeng.binding.service');
var primeng_binding_validation_component_1 = require('./primeng.binding.validation.component');
var moap_relation_selector_component_1 = require('../../components/autocomplete/moap.relation.selector.component');
var jsonapi_1 = require('../../ngrx/jsonapi');
exports.bingingServiceFactory = function (service, utils) {
	return new primeng_binding_service_1.NgrxPrimengBindingService(service, utils);
};
var primeng_binding_utils_1 = require('./primeng.binding.utils');
var NgrxPrimengBindingModule = (function () {
	function NgrxPrimengBindingModule() {
	}

	NgrxPrimengBindingModule.configure = function () {
		return {
			ngModule: jsonapi_1.NgrxJsonApiModule,
			providers: [
				{
					provide: primeng_binding_service_1.NgrxPrimengBindingService,
					useFactory: exports.bingingServiceFactory,
					deps: [jsonapi_1.NgrxJsonApiService, primeng_binding_utils_1.NgrxBindingUtils]
				},
				{
					provide: primeng_binding_utils_1.NgrxBindingUtils,
					useClass: primeng_binding_utils_1.NgrxBindingUtils
				}
			]
		};
	};
	NgrxPrimengBindingModule = __decorate([
		core_1.NgModule({
			imports: [
				jsonapi_1.NgrxJsonApiModule
			],
			exports: [
				primeng_binding_service_1.NgrxPrimengBindingService, primeng_binding_form_1.FormBinding,
				primeng_binding_selector_1.SelectorBinding, primeng_binding_table_1.DataTableBinding,
				primeng_binding_validation_component_1.ResourceErrorComponent,
				primeng_binding_validation_component_1.ResourceErrorsComponent
			],
			declarations: [primeng_binding_validation_component_1.ResourceErrorComponent,
				primeng_binding_validation_component_1.ResourceErrorsComponent,
				moap_relation_selector_component_1.ResourceRelationshipComponent]
		})
	], NgrxPrimengBindingModule);
	return NgrxPrimengBindingModule;
}());
exports.NgrxPrimengBindingModule = NgrxPrimengBindingModule;
