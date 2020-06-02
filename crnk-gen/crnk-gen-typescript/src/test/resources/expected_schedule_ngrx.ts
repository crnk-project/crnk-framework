import {
	BeanPath,
	BooleanPath,
	CrnkStoreResource,
	MapPath,
	QTypedManyResourceRelationship,
	QTypedOneResourceRelationship,
	StringPath
} from './crnk';
import {DefaultPagedMetaInformation} from './default.paged.meta.information';
import {DefaultPagedLinksInformation} from './information/default.paged.links.information';
import {
	Projects,
	QProjects
} from './projects';
import {
	QScheduleStatus,
	ScheduleStatus
} from './schedule.status';
import {
	QTasks,
	Tasks
} from './tasks';
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
		taskSet?: TypedManyResourceRelationship<Tasks>;
		project?: TypedOneResourceRelationship<Projects>;
		projects?: TypedManyResourceRelationship<Projects>;
		status?: TypedOneResourceRelationship<ScheduleStatus>;
	}
	export interface Attributes {
		name?: string;
		description?: string;
		delayed?: boolean;
		customData?: { [key: string]: string };
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
	export interface ScheduleListMeta extends DefaultPagedMetaInformation {
	}
}
export interface ScheduleListResult extends ManyQueryResult {
	data?: Array<Schedule>;
	links?: ScheduleListResult.ScheduleListLinks;
	meta?: ScheduleListResult.ScheduleListMeta;
}
export class QSchedule extends BeanPath<Schedule> {
	metaId = 'resources.schedule';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QSchedule.QRelationships = new QSchedule.QRelationships(this, 'relationships');
	attributes: QSchedule.QAttributes = new QSchedule.QAttributes(this, 'attributes');
}
export module QSchedule {
	export class QRelationships extends BeanPath<Schedule.Relationships> {
		private _taskSet: QTypedManyResourceRelationship<QTasks, Tasks>;
		get taskSet(): QTypedManyResourceRelationship<QTasks, Tasks> {
			if (!this._taskSet) {
				this._taskSet =
					new QTypedManyResourceRelationship<QTasks, Tasks>(this, 'taskSet', QTasks);
			}
			return this._taskSet;
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
		private _status: QTypedOneResourceRelationship<QScheduleStatus, ScheduleStatus>;
		get status(): QTypedOneResourceRelationship<QScheduleStatus, ScheduleStatus> {
			if (!this._status) {
				this._status =
					new QTypedOneResourceRelationship<QScheduleStatus, ScheduleStatus>(this, 'status', QScheduleStatus);
			}
			return this._status;
		};
	}
	export class QAttributes extends BeanPath<Schedule.Attributes> {
		name: StringPath = this.createString('name');
		description: StringPath = this.createString('description');
		delayed: BooleanPath = this.createBoolean('delayed');
		customData: MapPath<StringPath, string> = new MapPath(this, 'customData', StringPath);
	}
}
export let createEmptySchedule = function(id: string): Schedule {
	return {
		id: id,
		type: 'schedule',
		attributes: {
		},
		relationships: {
			taskSet: {data: []},
			project: {data: null},
			projects: {data: []},
			status: {data: null},
		},
	};
};