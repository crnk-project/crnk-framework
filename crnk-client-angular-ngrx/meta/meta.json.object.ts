import {BeanPath, StringPath} from '../expression/';
import {MetaDataObject, QMetaDataObject} from './meta.data.object';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export interface MetaJsonObject extends MetaDataObject {
}
export interface MetaJsonObjectResult extends OneQueryResult {
	data?: MetaJsonObject;
}
export interface MetaJsonObjectListResult extends ManyQueryResult {
	data?: Array<MetaJsonObject>;
}
export class QMetaJsonObject extends BeanPath<MetaJsonObject> {
	metaId = 'io.crnk.meta.resource.MetaJsonObject';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaDataObject.QRelationships = new QMetaDataObject.QRelationships(this, 'relationships');
	attributes: QMetaDataObject.QAttributes = new QMetaDataObject.QAttributes(this, 'attributes');
}
export let createEmptyMetaJsonObject = function(id: string): MetaJsonObject {
	return {
		id: id,
		type: 'meta/jsonObject',
	};
};