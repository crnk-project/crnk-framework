import {DefaultPagedLinksInformation} from './information/default.paged.links.information';
import {Projects} from './projects';
import {Tasks} from './tasks';
import {CrnkStoreResource} from '@crnk/angular-ngrx';
import {
	ManyQueryResult,
	OneQueryResult,
	ResourceRelationship,
	TypedManyResourceRelationship,
	TypedOneResourceRelationship
} from 'ngrx-json-api';

export module Schedules {
	export interface Relationships {
		[key: string]: ResourceRelationship;
		task?: TypedOneResourceRelationship<Tasks>;
		lazyTask?: TypedOneResourceRelationship<Tasks>;
		tasks?: TypedManyResourceRelationship<Tasks>;
		tasksList?: TypedManyResourceRelationship<Tasks>;
		project?: TypedOneResourceRelationship<Projects>;
		projects?: TypedManyResourceRelationship<Projects>;
	}
	export interface Attributes {
		name?: string;
		description?: string;
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
			project: {data: null},
			projects: {data: []},
		},
	};
};