import {Tasks} from './tasks';
import {DefaultPagedLinksInformation} from '@crnk/angular-ngrx/';
import {CrnkStoreResource} from '@crnk/angular-ngrx/stub';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedManyResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module Schedules {
	export interface Relationships {
		[key: string]: ResourceRelationship;
		task?: TypedOneResourceRelationship<Tasks>;
		lazyTask?: TypedOneResourceRelationship<Tasks>;
		tasks?: TypedManyResourceRelationship<Tasks>;
		tasksList?: TypedManyResourceRelationship<Tasks>;
	}
	export interface Attributes {
		name?: string;
		delayed?: boolean;
	}
}
export interface Schedules extends CrnkStoreResource {
	relationships?: Schedules.Relationships;
	attributes?: Schedules.Attributes;
}
export interface SchedulesResult extends OneQueryResult {
	data?: Schedules;
}
export module SchedulesListResult {
	export interface ScheduleListLinks extends DefaultPagedLinksInformation {
	}
	export interface ScheduleListMeta {
	}
}
export interface SchedulesListResult extends ManyQueryResult {
	data?: Array<Schedules>;
	links?: SchedulesListResult.ScheduleListLinks;
	meta?: SchedulesListResult.ScheduleListMeta;
}
export let createEmptySchedules = function(id: string): Schedules {
	return {
		id: id,
		type: 'schedules',
		attributes: {
		},
		relationships: {
			task: {data: null},
			lazyTask: {data: null},
			tasks: {data: []},
			tasksList: {data: []},
		},
	};
};