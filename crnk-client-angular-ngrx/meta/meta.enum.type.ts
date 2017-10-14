import {BeanPath, StringPath} from '../expression/';
import {QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export interface MetaEnumType extends MetaType {
}
export interface MetaEnumTypeResult extends OneQueryResult {
	data?: MetaEnumType;
}
export interface MetaEnumTypeListResult extends ManyQueryResult {
	data?: Array<MetaEnumType>;
}
export class QMetaEnumType extends BeanPath<MetaEnumType> {
	metaId = 'resources.meta.enumType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export let createEmptyMetaEnumType = function(id: string): MetaEnumType {
	return {
		id: id,
		type: 'meta/enumType',
	};
};