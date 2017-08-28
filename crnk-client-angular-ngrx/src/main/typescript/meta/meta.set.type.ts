import {BeanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaCollectionType} from './meta.collection.type';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaSetType {
	export interface Relationships extends MetaCollectionType.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaCollectionType.Attributes {
	}
}
export interface MetaSetType extends MetaCollectionType {
	relationships?: MetaSetType.Relationships;
	attributes?: MetaSetType.Attributes;
}
export interface MetaSetTypeResult extends OneQueryResult {
	data?: MetaSetType;
}
export interface MetaSetTypeListResult extends ManyQueryResult {
	data?: Array<MetaSetType>;
}
export class QMetaSetType extends BeanPath<MetaSetType> {
	metaId = 'io.crnk.meta.MetaSetType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaSetType.QRelationships = new QMetaSetType.QRelationships(this, 'relationships');
	attributes: QMetaSetType.QAttributes = new QMetaSetType.QAttributes(this, 'attributes');
}
export module QMetaSetType {
	export class QRelationships extends BeanPath<MetaSetType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaSetType.Attributes> {
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaSetType = function(id: string): MetaSetType {
	return {
		id: id,
		type: 'meta/setType',
		attributes: {
		},
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};