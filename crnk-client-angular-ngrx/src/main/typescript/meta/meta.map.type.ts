import {BeanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaMapType {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
		keyType?: TypedOneResourceRelationship<MetaType>;
	}
}
export interface MetaMapType extends MetaType {
	relationships?: MetaMapType.Relationships;
}
export interface MetaMapTypeResult extends OneQueryResult {
	data?: MetaMapType;
}
export interface MetaMapTypeListResult extends ManyQueryResult {
	data?: Array<MetaMapType>;
}
export class QMetaMapType extends BeanPath<MetaMapType> {
	metaId = 'io.crnk.meta.MetaMapType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaMapType.QRelationships = new QMetaMapType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaMapType {
	export class QRelationships extends BeanPath<MetaMapType.Relationships> {
		keyType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'keyType', QMetaType);
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
}
export let createEmptyMetaMapType = function(id: string): MetaMapType {
	return {
		id: id,
		type: 'meta/mapType',
		relationships: {
			keyType: {data: null},
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};