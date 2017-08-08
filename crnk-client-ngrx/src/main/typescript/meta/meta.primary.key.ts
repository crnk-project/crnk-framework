import {BeanPath, BooleanExpression} from '../expression/';
import {MetaKey, QMetaKey} from './meta.key';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export module MetaPrimaryKey {
	export interface Attributes {
		generated?;
	}
}
export interface MetaPrimaryKey extends MetaKey {
	attributes?: MetaPrimaryKey.Attributes;
}
export interface MetaPrimaryKeyResult extends OneQueryResult {
	data?: MetaPrimaryKey;
}
export interface MetaPrimaryKeyListResult extends ManyQueryResult {
	data?: Array<MetaPrimaryKey>;
}
export class QMetaPrimaryKey extends BeanPath<MetaPrimaryKey> {
	metaId = 'io.crnk.meta.MetaPrimaryKey';
	attributes: QMetaPrimaryKey.QAttributes = new QMetaPrimaryKey.QAttributes(this, 'attributes');
	relationships: QMetaKey.QRelationships = new QMetaKey.QRelationships(this, 'relationships');
}
export module QMetaPrimaryKey {
	export class QAttributes extends BeanPath<MetaPrimaryKey.Attributes> {
		generated: BooleanExpression = this.createBoolean('generated');
	}
}
export let createEmptyMetaPrimaryKey = function(id: string): MetaPrimaryKey {
	return {
		id: id,
		type: 'meta/primaryKey',
		attributes: {
		},
	};
};