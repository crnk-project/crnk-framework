import {BeanPath, StringExpression} from '../expression/'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/'
import {MetaElement, QMetaElement} from './meta.element'
import {MetaType, QMetaType} from './meta.type'
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module MetaArrayType {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaType.Attributes {
	}
}
export interface MetaArrayType extends MetaType {
	relationships?: MetaArrayType.Relationships;
	attributes?: MetaArrayType.Attributes;
}
export interface MetaArrayTypeResult extends OneQueryResult {
	data?: MetaArrayType;
}
export interface MetaArrayTypeListResult extends ManyQueryResult {
	data?: Array<MetaArrayType>;
}
export class QMetaArrayType extends BeanPath<MetaArrayType> {
	metaId: string = 'io.crnk.meta.MetaArrayType';
	relationships: QMetaArrayType.QRelationships = new QMetaArrayType.QRelationships(this, 'relationships');
	attributes: QMetaArrayType.QAttributes = new QMetaArrayType.QAttributes(this, 'attributes');
}
export module QMetaArrayType {
	export class QRelationships extends BeanPath<MetaArrayType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaArrayType.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaArrayType = function(id: string): MetaArrayType {
	return {
		id: id,
		type: 'meta/arrayType',
		attributes: {
		},
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};