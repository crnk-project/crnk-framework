import {BeanPath, StringExpression} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaEnumType {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaType.Attributes {
	}
}
export interface MetaEnumType extends MetaType {
	relationships?: MetaEnumType.Relationships;
	attributes?: MetaEnumType.Attributes;
}
export interface MetaEnumTypeResult extends OneQueryResult {
	data?: MetaEnumType;
}
export interface MetaEnumTypeListResult extends ManyQueryResult {
	data?: Array<MetaEnumType>;
}
export class QMetaEnumType extends BeanPath<MetaEnumType> {
	metaId = 'io.crnk.meta.MetaEnumType';
	relationships: QMetaEnumType.QRelationships = new QMetaEnumType.QRelationships(this, 'relationships');
	attributes: QMetaEnumType.QAttributes = new QMetaEnumType.QAttributes(this, 'attributes');
}
export module QMetaEnumType {
	export class QRelationships extends BeanPath<MetaEnumType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaEnumType.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaEnumType = function(id: string): MetaEnumType {
	return {
		id: id,
		type: 'meta/enumType',
		attributes: {
		},
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};