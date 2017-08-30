import {BeanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaType {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
		elementType?: TypedOneResourceRelationship<MetaType>;
	}
}
export interface MetaType extends MetaElement {
	relationships?: MetaType.Relationships;
}
export interface MetaTypeResult extends OneQueryResult {
	data?: MetaType;
}
export interface MetaTypeListResult extends ManyQueryResult {
	data?: Array<MetaType>;
}
export class QMetaType extends BeanPath<MetaType> {
	metaId = 'io.crnk.meta.MetaType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaType {
	export class QRelationships extends BeanPath<MetaType.Relationships> {
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
}
export let createEmptyMetaType = function(id: string): MetaType {
	return {
		id: id,
		type: 'meta/type',
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};