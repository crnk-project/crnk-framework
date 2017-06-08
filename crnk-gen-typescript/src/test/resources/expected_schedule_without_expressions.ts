import {Task} from './task'
import {DefaultPagedLinksInformation} from '@crnk/core/'
import {ManyQueryResult, OneQueryResult, ResourceRelationship, StoreResource, TypedManyResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module Schedule {
	export interface Relationships {
		[key: string]: ResourceRelationship;
		task?: TypedOneResourceRelationship<Task>;
		lazyTask?: TypedOneResourceRelationship<Task>;
		tasks?: TypedManyResourceRelationship<Task>;
		tasksList?: TypedManyResourceRelationship<Task>;
	}
	export interface Attributes {
		name?: string;
		delayed?: boolean;
	}
}
export interface Schedule extends StoreResource {
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