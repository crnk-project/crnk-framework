import {
	ManyResourceRelationship,
	ManyResult,
	OneResourceRelationship,
	OneResult,
	Resource
} from './crnk';
import {DefaultPagedMetaInformation} from './default.paged.meta.information';
import {DefaultPagedLinksInformation} from './information/default.paged.links.information';
import {Projects} from './projects';
import {Tasks} from './tasks';

export interface Schedule extends Resource {
	name?: string;
	description?: string;
	task?: OneResourceRelationship<Tasks>;
	lazyTask?: OneResourceRelationship<Tasks>;
	tasks?: ManyResourceRelationship<Tasks>;
	tasksList?: ManyResourceRelationship<Tasks>;
	project?: OneResourceRelationship<Projects>;
	projects?: ManyResourceRelationship<Projects>;
	delayed?: boolean;
	customData?: { [key: string]: string };
}
export interface ScheduleResult extends OneResult {
	data?: Schedule;
}
export module ScheduleListResult {
	export interface ScheduleListLinks extends DefaultPagedLinksInformation {
	}
	export interface ScheduleListMeta extends DefaultPagedMetaInformation {
	}
}
export interface ScheduleListResult extends ManyResult {
	data?: Array<Schedule>;
	links?: ScheduleListResult.ScheduleListLinks;
	meta?: ScheduleListResult.ScheduleListMeta;
}
export let createEmptySchedule = function(id: string): Schedule {
	return {
		id: id,
		type: 'schedule',
	};
};