import {BeanPath, StringExpression} from '../expression/'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/'
import {MetaElement, QMetaElement} from './meta.element'
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module MetaLiteral {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaElement.Attributes {
	}
}
export interface MetaLiteral extends MetaElement {
	relationships?: MetaLiteral.Relationships;
	attributes?: MetaLiteral.Attributes;
}
export interface MetaLiteralResult extends OneQueryResult {
	data?: MetaLiteral;
}
export interface MetaLiteralListResult extends ManyQueryResult {
	data?: Array<MetaLiteral>;
}
export class QMetaLiteral extends BeanPath<MetaLiteral> {
	metaId: string = 'io.crnk.meta.MetaLiteral';
	relationships: QMetaLiteral.QRelationships = new QMetaLiteral.QRelationships(this, 'relationships');
	attributes: QMetaLiteral.QAttributes = new QMetaLiteral.QAttributes(this, 'attributes');
}
export module QMetaLiteral {
	export class QRelationships extends BeanPath<MetaLiteral.Relationships> {
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaLiteral.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaLiteral = function(id: string): MetaLiteral {
	return {
		id: id,
		type: 'meta/enumLiteral',
		attributes: {
		},
		relationships: {
			parent: {data: null},
			children: {data: []},
		},
	};
};