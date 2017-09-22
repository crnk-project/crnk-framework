import {QTask, Task} from './task';
import {DefaultPagedLinksInformation} from '@crnk/angular-ngrx/';
import {BeanPath, BooleanPath, StringPath} from '@crnk/angular-ngrx/expression';
import {CrnkStoreResource, QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '@crnk/angular-ngrx/stub';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedManyResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

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
export class QSchedule extends BeanPath<Schedule> {
	metaId = 'io.crnk.test.mock.models.Schedule';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QSchedule.QRelationships = new QSchedule.QRelationships(this, 'relationships');
	attributes: QSchedule.QAttributes = new QSchedule.QAttributes(this, 'attributes');
}
export module QSchedule {
	export class QRelationships extends BeanPath<Schedule.Relationships> {
		private _task: QTypedOneResourceRelationship<QTask, Task>;
		get task(): QTypedOneResourceRelationship<QTask, Task> {
			if(!this._task){this._task= new QTypedOneResourceRelationship<QTask, Task>(this, 'task', QTask);}
			return this._task
		};
		private _lazyTask: QTypedOneResourceRelationship<QTask, Task>;
		get lazyTask(): QTypedOneResourceRelationship<QTask, Task> {
			if(!this._lazyTask){this._lazyTask= new QTypedOneResourceRelationship<QTask, Task>(this, 'lazyTask', QTask);}
			return this._lazyTask
		};
		private _tasks: QTypedManyResourceRelationship<QTask, Task>;
		get tasks(): QTypedManyResourceRelationship<QTask, Task> {
			if(!this._tasks){this._tasks= new QTypedManyResourceRelationship<QTask, Task>(this, 'tasks', QTask);}
			return this._tasks
		};
		private _tasksList: QTypedManyResourceRelationship<QTask, Task>;
		get tasksList(): QTypedManyResourceRelationship<QTask, Task> {
			if(!this._tasksList){this._tasksList= new QTypedManyResourceRelationship<QTask, Task>(this, 'tasksList', QTask);}
			return this._tasksList
		};
	}
	export class QAttributes extends BeanPath<Schedule.Attributes> {
		name: StringPath = this.createString('name');
		delayed: BooleanPath = this.createBoolean('delayed');
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