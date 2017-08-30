import {BeanPath, StringPath} from '../expression/';
import {CrnkStoreResource, QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedManyResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaElement {
	export interface Relationships {
		[key: string]: ResourceRelationship;
		parent?: TypedOneResourceRelationship<MetaElement>;
		children?: TypedManyResourceRelationship<MetaElement>;
	}
	export interface Attributes {
		name?: string;
	}
}
export interface MetaElement extends CrnkStoreResource {
	relationships?: MetaElement.Relationships;
	attributes?: MetaElement.Attributes;
}
export interface MetaElementResult extends OneQueryResult {
	data?: MetaElement;
}
export interface MetaElementListResult extends ManyQueryResult {
	data?: Array<MetaElement>;
}
export class QMetaElement extends BeanPath<MetaElement> {
	metaId = 'io.crnk.meta.MetaElement';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaElement.QRelationships = new QMetaElement.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaElement {
	export class QAttributes extends BeanPath<MetaElement.Attributes> {
		name: StringPath = this.createString('name');
	}
	export class QRelationships extends BeanPath<MetaElement.Relationships> {
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
}
export let createEmptyMetaElement = function(id: string): MetaElement {
	return {
		id: id,
		type: 'meta/element',
		attributes: {
		},
		relationships: {
			parent: {data: null},
			children: {data: []},
		},
	};
};