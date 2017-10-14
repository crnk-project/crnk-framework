import {BeanPath, StringPath} from '../expression/';
import {MetaCollectionType} from './meta.collection.type';
import {QMetaElement} from './meta.element';
import {QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export interface MetaListType extends MetaCollectionType {
}
export interface MetaListTypeResult extends OneQueryResult {
	data?: MetaListType;
}
export interface MetaListTypeListResult extends ManyQueryResult {
	data?: Array<MetaListType>;
}
export class QMetaListType extends BeanPath<MetaListType> {
	metaId = 'resources.meta.listType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export let createEmptyMetaListType = function(id: string): MetaListType {
	return {
		id: id,
		type: 'meta/listType',
	};
};