import {BeanPath, StringPath} from '../expression/';
import {MetaElement, QMetaElement} from './meta.element';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api';

export interface MetaLiteral extends MetaElement {
}
export interface MetaLiteralResult extends OneQueryResult {
	data?: MetaLiteral;
}
export interface MetaLiteralListResult extends ManyQueryResult {
	data?: Array<MetaLiteral>;
}
export class QMetaLiteral extends BeanPath<MetaLiteral> {
	metaId = 'io.crnk.meta.MetaLiteral';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaElement.QRelationships = new QMetaElement.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export let createEmptyMetaLiteral = function(id: string): MetaLiteral {
	return {
		id: id,
		type: 'meta/enumLiteral',
	};
};