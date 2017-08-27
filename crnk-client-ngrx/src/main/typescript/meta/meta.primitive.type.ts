import {BeanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaPrimitiveType {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaType.Attributes {
	}
}
export interface MetaPrimitiveType extends MetaType {
	relationships?: MetaPrimitiveType.Relationships;
	attributes?: MetaPrimitiveType.Attributes;
}
export interface MetaPrimitiveTypeResult extends OneQueryResult {
	data?: MetaPrimitiveType;
}
export interface MetaPrimitiveTypeListResult extends ManyQueryResult {
	data?: Array<MetaPrimitiveType>;
}
export class QMetaPrimitiveType extends BeanPath<MetaPrimitiveType> {
	metaId = 'io.crnk.meta.MetaPrimitiveType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaPrimitiveType.QRelationships = new QMetaPrimitiveType.QRelationships(this, 'relationships');
	attributes: QMetaPrimitiveType.QAttributes = new QMetaPrimitiveType.QAttributes(this, 'attributes');
}
export module QMetaPrimitiveType {
	export class QRelationships extends BeanPath<MetaPrimitiveType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaPrimitiveType.Attributes> {
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaPrimitiveType = function(id: string): MetaPrimitiveType {
	return {
		id: id,
		type: 'meta/primitiveType',
		attributes: {
		},
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};