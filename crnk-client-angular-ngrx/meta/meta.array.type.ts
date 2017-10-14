import {BeanPath, StringPath} from '../expression/';
import {QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export interface MetaArrayType extends MetaType {
}
export interface MetaArrayTypeResult extends OneQueryResult {
	data?: MetaArrayType;
}
export interface MetaArrayTypeListResult extends ManyQueryResult {
	data?: Array<MetaArrayType>;
}
export class QMetaArrayType extends BeanPath<MetaArrayType> {
	metaId = 'resources.meta.arrayType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export let createEmptyMetaArrayType = function(id: string): MetaArrayType {
	return {
		id: id,
		type: 'meta/arrayType',
	};
};