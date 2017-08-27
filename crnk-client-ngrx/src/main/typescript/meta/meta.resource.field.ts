import {BeanPath, BooleanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaAttribute, QMetaAttribute} from './meta.attribute';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaResourceField {
	export interface Relationships extends MetaAttribute.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaAttribute.Attributes {
		meta?: boolean;
		links?: boolean;
	}
}
export interface MetaResourceField extends MetaAttribute {
	relationships?: MetaResourceField.Relationships;
	attributes?: MetaResourceField.Attributes;
}
export interface MetaResourceFieldResult extends OneQueryResult {
	data?: MetaResourceField;
}
export interface MetaResourceFieldListResult extends ManyQueryResult {
	data?: Array<MetaResourceField>;
}
export class QMetaResourceField extends BeanPath<MetaResourceField> {
	metaId = 'io.crnk.meta.resource.MetaResourceField';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaResourceField.QRelationships = new QMetaResourceField.QRelationships(this, 'relationships');
	attributes: QMetaResourceField.QAttributes = new QMetaResourceField.QAttributes(this, 'attributes');
}
export module QMetaResourceField {
	export class QRelationships extends BeanPath<MetaResourceField.Relationships> {
		type: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'type', QMetaType);
		oppositeAttribute: QTypedOneResourceRelationship<QMetaAttribute, MetaAttribute> = new QTypedOneResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'oppositeAttribute', QMetaAttribute);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaResourceField.Attributes> {
		meta: BooleanPath = this.createBoolean('meta');
		links: BooleanPath = this.createBoolean('links');
		association: BooleanPath = this.createBoolean('association');
		derived: BooleanPath = this.createBoolean('derived');
		lazy: BooleanPath = this.createBoolean('lazy');
		version: BooleanPath = this.createBoolean('version');
		primaryKeyAttribute: BooleanPath = this.createBoolean('primaryKeyAttribute');
		sortable: BooleanPath = this.createBoolean('sortable');
		filterable: BooleanPath = this.createBoolean('filterable');
		insertable: BooleanPath = this.createBoolean('insertable');
		updatable: BooleanPath = this.createBoolean('updatable');
		lob: BooleanPath = this.createBoolean('lob');
		nullable: BooleanPath = this.createBoolean('nullable');
		cascaded: BooleanPath = this.createBoolean('cascaded');
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaResourceField = function(id: string): MetaResourceField {
	return {
		id: id,
		type: 'meta/resourceField',
		attributes: {
		},
		relationships: {
			type: {data: null},
			oppositeAttribute: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};