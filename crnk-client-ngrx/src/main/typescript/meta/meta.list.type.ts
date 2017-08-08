import {BeanPath, StringExpression} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaCollectionType} from './meta.collection.type';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaListType {
	export interface Relationships extends MetaCollectionType.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaCollectionType.Attributes {
	}
}
export interface MetaListType extends MetaCollectionType {
	relationships?: MetaListType.Relationships;
	attributes?: MetaListType.Attributes;
}
export interface MetaListTypeResult extends OneQueryResult {
	data?: MetaListType;
}
export interface MetaListTypeListResult extends ManyQueryResult {
	data?: Array<MetaListType>;
}
export class QMetaListType extends BeanPath<MetaListType> {
	metaId = 'io.crnk.meta.MetaListType';
	relationships: QMetaListType.QRelationships = new QMetaListType.QRelationships(this, 'relationships');
	attributes: QMetaListType.QAttributes = new QMetaListType.QAttributes(this, 'attributes');
}
export module QMetaListType {
	export class QRelationships extends BeanPath<MetaListType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaListType.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaListType = function(id: string): MetaListType {
	return {
		id: id,
		type: 'meta/listType',
		attributes: {
		},
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};