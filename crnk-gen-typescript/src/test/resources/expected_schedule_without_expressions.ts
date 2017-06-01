import {Task} from './task'
import {ResourceRelationship, StoreResource, TypedManyResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api'

export module Schedule{
	export interface Relationships{
		[key: string]: ResourceRelationship;
		task?: TypedOneResourceRelationship<Task>;
		lazyTask?: TypedOneResourceRelationship<Task>;
		tasks?: TypedManyResourceRelationship<Task>;
		tasksList?: TypedManyResourceRelationship<Task>;
	}
	export interface Attributes{
		name?: string;
		delayed?: boolean;
	}
}
export interface Schedule extends StoreResource{
	relationships?: Schedule.Relationships;
	attributes?: Schedule.Attributes;
}