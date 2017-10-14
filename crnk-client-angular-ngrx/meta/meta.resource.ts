import {BeanPath, BooleanPath, StringPath} from '../expression/';
import {MetaDataObject, QMetaDataObject} from './meta.data.object';
import {MetaResourceBase} from './meta.resource.base';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export module MetaResource {
	export interface Attributes extends MetaDataObject.Attributes {
		resourceType?: string;
	}
}
export interface MetaResource extends MetaResourceBase {
	attributes?: MetaResource.Attributes;
}
export interface MetaResourceResult extends OneQueryResult {
	data?: MetaResource;
}
export interface MetaResourceListResult extends ManyQueryResult {
	data?: Array<MetaResource>;
}
export class QMetaResource extends BeanPath<MetaResource> {
	metaId = 'resources.meta.resource';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	attributes: QMetaResource.QAttributes = new QMetaResource.QAttributes(this, 'attributes');
	relationships: QMetaDataObject.QRelationships = new QMetaDataObject.QRelationships(this, 'relationships');
}
export module QMetaResource {
	export class QAttributes extends BeanPath<MetaResource.Attributes> {
		resourceType: StringPath = this.createString('resourceType');
		insertable: BooleanPath = this.createBoolean('insertable');
		updatable: BooleanPath = this.createBoolean('updatable');
		deletable: BooleanPath = this.createBoolean('deletable');
		readable: BooleanPath = this.createBoolean('readable');
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaResource = function(id: string): MetaResource {
	return {
		id: id,
		type: 'meta/resource',
		attributes: {
		},
	};
};