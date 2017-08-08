import {BeanPath, StringExpression} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaCollectionType {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaType.Attributes {
	}
}
export interface MetaCollectionType extends MetaType {
	relationships?: MetaCollectionType.Relationships;
	attributes?: MetaCollectionType.Attributes;
}
export interface MetaCollectionTypeResult extends OneQueryResult {
	data?: MetaCollectionType;
}
export interface MetaCollectionTypeListResult extends ManyQueryResult {
	data?: Array<MetaCollectionType>;
}
export class QMetaCollectionType extends BeanPath<MetaCollectionType> {
	metaId = 'io.crnk.meta.MetaCollectionType';
	relationships: QMetaCollectionType.QRelationships = new QMetaCollectionType.QRelationships(this, 'relationships');
	attributes: QMetaCollectionType.QAttributes = new QMetaCollectionType.QAttributes(this, 'attributes');
}
export module QMetaCollectionType {
	export class QRelationships extends BeanPath<MetaCollectionType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaCollectionType.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaCollectionType = function(id: string): MetaCollectionType {
	return {
		id: id,
		type: 'meta/collectionType',
		attributes: {
		},
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};