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

export module Schedule {
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
export interface Schedule extends CrnkStoreResource {
	relationships?: Schedule.Relationships;
	attributes?: Schedule.Attributes;
}
export interface ScheduleResult extends OneQueryResult {
	data?: Schedule;
}
export module ScheduleListResult {
	export interface ScheduleListLinks extends DefaultPagedLinksInformation {
	}
	export interface ScheduleListMeta {
	}
}
export interface ScheduleListResult extends ManyQueryResult {
	data?: Array<Schedule>;
	links?: ScheduleListResult.ScheduleListLinks;
	meta?: ScheduleListResult.ScheduleListMeta;
}
export let createEmptySchedule = function(id: string): Schedule {
	return {
		id: id,
		type: 'schedule',
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