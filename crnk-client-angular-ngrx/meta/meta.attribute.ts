import {BeanPath, BooleanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaAttribute {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
		type?: TypedOneResourceRelationship<MetaType>;
		oppositeAttribute?: TypedOneResourceRelationship<MetaAttribute>;
	}
	export interface Attributes extends MetaElement.Attributes {
		association?: boolean;
		derived?: boolean;
		lazy?: boolean;
		version?: boolean;
		primaryKeyAttribute?: boolean;
		sortable?: boolean;
		filterable?: boolean;
		insertable?: boolean;
		updatable?: boolean;
		lob?: boolean;
		nullable?: boolean;
		cascaded?: boolean;
	}
}
export interface MetaAttribute extends MetaElement {
	relationships?: MetaAttribute.Relationships;
	attributes?: MetaAttribute.Attributes;
}
export interface MetaAttributeResult extends OneQueryResult {
	data?: MetaAttribute;
}
export interface MetaAttributeListResult extends ManyQueryResult {
	data?: Array<MetaAttribute>;
}
export class QMetaAttribute extends BeanPath<MetaAttribute> {
	metaId = 'io.crnk.meta.MetaAttribute';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaAttribute.QRelationships = new QMetaAttribute.QRelationships(this, 'relationships');
	attributes: QMetaAttribute.QAttributes = new QMetaAttribute.QAttributes(this, 'attributes');
}
export module QMetaAttribute {
	export class QRelationships extends BeanPath<MetaAttribute.Relationships> {
		type: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'type', QMetaType);
		oppositeAttribute: QTypedOneResourceRelationship<QMetaAttribute, MetaAttribute> = new QTypedOneResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'oppositeAttribute', QMetaAttribute);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaAttribute.Attributes> {
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
export let createEmptyMetaAttribute = function(id: string): MetaAttribute {
	return {
		id: id,
		type: 'meta/attribute',
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