import {BeanPath, BooleanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaAttribute, QMetaAttribute} from './meta.attribute';
import {MetaElement, QMetaElement} from './meta.element';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedManyResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaKey {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
		elements?: TypedManyResourceRelationship<MetaAttribute>;
	}
	export interface Attributes extends MetaElement.Attributes {
		unique?: boolean;
	}
}
export interface MetaKey extends MetaElement {
	relationships?: MetaKey.Relationships;
	attributes?: MetaKey.Attributes;
}
export interface MetaKeyResult extends OneQueryResult {
	data?: MetaKey;
}
export interface MetaKeyListResult extends ManyQueryResult {
	data?: Array<MetaKey>;
}
export class QMetaKey extends BeanPath<MetaKey> {
	metaId = 'io.crnk.meta.MetaKey';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaKey.QRelationships = new QMetaKey.QRelationships(this, 'relationships');
	attributes: QMetaKey.QAttributes = new QMetaKey.QAttributes(this, 'attributes');
}
export module QMetaKey {
	export class QRelationships extends BeanPath<MetaKey.Relationships> {
		elements: QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute> = new QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'elements', QMetaAttribute);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaKey.Attributes> {
		unique: BooleanPath = this.createBoolean('unique');
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaKey = function(id: string): MetaKey {
	return {
		id: id,
		type: 'meta/key',
		attributes: {
		},
		relationships: {
			elements: {data: []},
			parent: {data: null},
			children: {data: []},
		},
	};
};