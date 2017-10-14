import {BeanPath, StringPath} from '../expression/';
import {QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export interface MetaPrimitiveType extends MetaType {
}
export interface MetaPrimitiveTypeResult extends OneQueryResult {
	data?: MetaPrimitiveType;
}
export interface MetaPrimitiveTypeListResult extends ManyQueryResult {
	data?: Array<MetaPrimitiveType>;
}
export class QMetaPrimitiveType extends BeanPath<MetaPrimitiveType> {
	metaId = 'resources.meta.primitiveType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export let createEmptyMetaPrimitiveType = function(id: string): MetaPrimitiveType {
	return {
		id: id,
		type: 'meta/primitiveType',
	};
};