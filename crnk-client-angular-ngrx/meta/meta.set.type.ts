import {BeanPath, StringPath} from '../expression/';
import {MetaCollectionType} from './meta.collection.type';
import {QMetaElement} from './meta.element';
import {QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export interface MetaSetType extends MetaCollectionType {
}
export interface MetaSetTypeResult extends OneQueryResult {
	data?: MetaSetType;
}
export interface MetaSetTypeListResult extends ManyQueryResult {
	data?: Array<MetaSetType>;
}
export class QMetaSetType extends BeanPath<MetaSetType> {
	metaId = 'resources.meta.setType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export let createEmptyMetaSetType = function(id: string): MetaSetType {
	return {
		id: id,
		type: 'meta/setType',
	};
};