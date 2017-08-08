import {BeanPath, BooleanExpression, StringExpression} from '../expression/';
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
		meta?;
		links?;
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
		meta: BooleanExpression = this.createBoolean('meta');
		links: BooleanExpression = this.createBoolean('links');
		association: BooleanExpression = this.createBoolean('association');
		derived: BooleanExpression = this.createBoolean('derived');
		lazy: BooleanExpression = this.createBoolean('lazy');
		version: BooleanExpression = this.createBoolean('version');
		primaryKeyAttribute: BooleanExpression = this.createBoolean('primaryKeyAttribute');
		sortable: BooleanExpression = this.createBoolean('sortable');
		filterable: BooleanExpression = this.createBoolean('filterable');
		insertable: BooleanExpression = this.createBoolean('insertable');
		updatable: BooleanExpression = this.createBoolean('updatable');
		lob: BooleanExpression = this.createBoolean('lob');
		nullable: BooleanExpression = this.createBoolean('nullable');
		cascaded: BooleanExpression = this.createBoolean('cascaded');
		name: StringExpression = this.createString('name');
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