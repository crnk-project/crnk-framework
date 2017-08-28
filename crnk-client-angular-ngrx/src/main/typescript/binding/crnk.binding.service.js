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
var NgrxPrimengBindingService = (function () {
	function NgrxPrimengBindingService(ngrxJsonApiService, utils) {
		this.ngrxJsonApiService = ngrxJsonApiService;
		this.utils = utils;
	}

	NgrxPrimengBindingService.prototype.bindDataTable = function (config) {
		return new primeng_binding_table_1.DataTableBinding(this.ngrxJsonApiService, config, this.utils);
	};
	NgrxPrimengBindingService.prototype.bindForm = function (config) {
		return new primeng_binding_form_1.FormBinding(this.ngrxJsonApiService, config, this.utils);
	};
	NgrxPrimengBindingService.prototype.bindSelector = function (config) {
		return new primeng_binding_selector_1.SelectorBinding(this.ngrxJsonApiService, config, this.utils);
	};
	NgrxPrimengBindingService = __decorate([
		core_1.Injectable()
	], NgrxPrimengBindingService);
	return NgrxPrimengBindingService;
}());
exports.NgrxPrimengBindingService = NgrxPrimengBindingService;
