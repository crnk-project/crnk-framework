import {QTask, Task} from './task'
import {DefaultPagedLinksInformation} from '@crnk/core/'
import {BeanPath, BooleanExpression, StringExpression} from '@crnk/ngrx/binding/expression'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '@crnk/ngrx/binding/stub'
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
export class QSchedule extends BeanPath<Schedule> {
	metaId: string = 'io.crnk.test.mock.models.Schedule';
	relationships: QSchedule.QRelationships = new QSchedule.QRelationships(this, 'relationships');
	attributes: QSchedule.QAttributes = new QSchedule.QAttributes(this, 'attributes');
}
export module QSchedule {
	export class QRelationships extends BeanPath<Schedule.Relationships> {
		task: QTypedOneResourceRelationship<QTask, Task> = new QTypedOneResourceRelationship<QTask, Task>(this, 'task', QTask);
		lazyTask: QTypedOneResourceRelationship<QTask, Task> = new QTypedOneResourceRelationship<QTask, Task>(this, 'lazyTask', QTask);
		tasks: QTypedManyResourceRelationship<QTask, Task> = new QTypedManyResourceRelationship<QTask, Task>(this, 'tasks', QTask);
		tasksList: QTypedManyResourceRelationship<QTask, Task> = new QTypedManyResourceRelationship<QTask, Task>(this, 'tasksList', QTask);
	}
	export class QAttributes extends BeanPath<Schedule.Attributes> {
		name: StringExpression = this.createString('name');
		delayed: BooleanExpression = this.createBoolean('delayed');
	}
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