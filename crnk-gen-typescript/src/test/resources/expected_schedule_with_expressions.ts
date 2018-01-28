import {DefaultPagedLinksInformation} from './information/default.paged.links.information';
import {
	Projects,
	QProjects
} from './projects';
import {
	QTasks,
	Tasks
} from './tasks';
import {
	BeanPath,
	BooleanPath,
	CrnkStoreResource,
	QTypedManyResourceRelationship,
	QTypedOneResourceRelationship,
	StringPath
} from '@crnk/angular-ngrx';
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
export class QSchedules extends BeanPath<Schedules> {
	metaId = 'resources.schedules';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QSchedules.QRelationships = new QSchedules.QRelationships(this, 'relationships');
	attributes: QSchedules.QAttributes = new QSchedules.QAttributes(this, 'attributes');
}
export module QSchedules {
	export class QRelationships extends BeanPath<Schedules.Relationships> {
		private _task: QTypedOneResourceRelationship<QTasks, Tasks>;
		get task(): QTypedOneResourceRelationship<QTasks, Tasks> {
			if (!this._task) {
				this._task =
					new QTypedOneResourceRelationship<QTasks, Tasks>(this, 'task', QTasks);
			}
			return this._task;
		};
		private _lazyTask: QTypedOneResourceRelationship<QTasks, Tasks>;
		get lazyTask(): QTypedOneResourceRelationship<QTasks, Tasks> {
			if (!this._lazyTask) {
				this._lazyTask =
					new QTypedOneResourceRelationship<QTasks, Tasks>(this, 'lazyTask', QTasks);
			}
			return this._lazyTask;
		};
		private _tasks: QTypedManyResourceRelationship<QTasks, Tasks>;
		get tasks(): QTypedManyResourceRelationship<QTasks, Tasks> {
			if (!this._tasks) {
				this._tasks =
					new QTypedManyResourceRelationship<QTasks, Tasks>(this, 'tasks', QTasks);
			}
			return this._tasks;
		};
		private _tasksList: QTypedManyResourceRelationship<QTasks, Tasks>;
		get tasksList(): QTypedManyResourceRelationship<QTasks, Tasks> {
			if (!this._tasksList) {
				this._tasksList =
					new QTypedManyResourceRelationship<QTasks, Tasks>(this, 'tasksList', QTasks);
			}
			return this._tasksList;
		};
		private _project: QTypedOneResourceRelationship<QProjects, Projects>;
		get project(): QTypedOneResourceRelationship<QProjects, Projects> {
			if (!this._project) {
				this._project =
					new QTypedOneResourceRelationship<QProjects, Projects>(this, 'project', QProjects);
			}
			return this._project;
		};
		private _projects: QTypedManyResourceRelationship<QProjects, Projects>;
		get projects(): QTypedManyResourceRelationship<QProjects, Projects> {
			if (!this._projects) {
				this._projects =
					new QTypedManyResourceRelationship<QProjects, Projects>(this, 'projects', QProjects);
			}
			return this._projects;
		};
	}
	export class QAttributes extends BeanPath<Schedules.Attributes> {
		name: StringPath = this.createString('name');
		delayed: BooleanPath = this.createBoolean('delayed');
	}
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