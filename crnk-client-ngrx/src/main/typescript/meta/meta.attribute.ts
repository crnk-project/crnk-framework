import {BeanPath, BooleanExpression, StringExpression} from '../expression/';
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
		association?;
		derived?;
		lazy?;
		version?;
		primaryKeyAttribute?;
		sortable?;
		filterable?;
		insertable?;
		updatable?;
		lob?;
		nullable?;
		cascaded?;
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