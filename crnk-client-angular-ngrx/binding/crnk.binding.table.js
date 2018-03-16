"use strict";
var Subject_1 = require('rxjs/Subject');
var _ = require('lodash');
require('rxjs/add/operator/zip');
require('rxjs/add/operator/do');
require('rxjs/add/operator/debounceTime');
require('rxjs/add/operator/distinct');
require('rxjs/add/operator/switch');
var jsonapi_1 = require('../../ngrx/jsonapi');
var DataTableBinding = (function () {
	function DataTableBinding(ngrxJsonApiService, config, utils) {
		var _this = this;
		this.ngrxJsonApiService = ngrxJsonApiService;
		this.config = config;
		this.utils = utils;
		this.resultsSubject = new Subject_1.Subject();
		this.resources = [];
		this.totalRecords = 0;
		this.latestQuery = null;
		this.num = 0;
		this.resultsSubject
			.switch()
			.filter(function (it) {
				return it != null;
			})
			.subscribe(function (resources) {
				_this.resources = resources;
				// TODO get meta information
				_this.totalRecords = resources.length;
			});
	}

	DataTableBinding.prototype.unsubscribe = function () {
		this.resultsSubject.unsubscribe();
	};
	DataTableBinding.prototype.onLazyLoad = function (event) {
		var offset = event.first;
		var limit = event.rows;
		var filters = [];
		var sorting = [];
		var includes = [];
		if (event.multiSortMeta) {
			for (var _i = 0, _a = event.multiSortMeta; _i < _a.length; _i++) {
				var sortMeta = _a[_i];
				var direction = sortMeta.order == -1 ? jsonapi_1.Direction.DESC : jsonapi_1.Direction.ASC;
				sorting.push({direction: direction, api: this.utils.toQueryPath(sortMeta.field)});
			}
		}
		else if (event.sortField) {
			var direction = event.sortOrder == -1 ? jsonapi_1.Direction.DESC : jsonapi_1.Direction.ASC;
			sorting.push({direction: direction, api: this.utils.toQueryPath(event.sortField)});
		}
		if (event.filters) {
			for (var filterKey in event.filters) {
				var filterMeta = event.filters[filterKey];
				var matchMode = filterMeta.matchMode;
				var value = filterMeta.value;
				if (!matchMode) {
					matchMode = 'like';
				}
				if (matchMode == 'like') {
					value = '%' + value + '%';
				}
				var attributePath = this.utils.toQueryPath(filterKey);
				filters.push({value: value, path: attributePath, operator: matchMode});
			}
		}
		var query = _.cloneDeep(this.config.query);
		this.utils.updateQueryParams(query, {
			limit: limit,
			offset: offset,
			include: includes,
			sorting: sorting,
			filtering: filters
		});
		this.num++;
		if (!_.isEqual(query, this.latestQuery)) {
			var resultObservable = this.ngrxJsonApiService.findMany(query, true, true);
			this.resultsSubject.next(resultObservable);
			this.latestQuery = query;
		}
	};
	return DataTableBinding;
}());
exports.DataTableBinding = DataTableBinding;
