import {BeanPath, BooleanPath, StringPath} from '../expression/';
import {MetaKey, QMetaKey} from './meta.key';
import {ManyQueryResult, OneQueryResult} from 'ngrx-json-api/src/interfaces';

export module MetaPrimaryKey {
	export interface Attributes extends MetaKey.Attributes {
		generated?: boolean;
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
	metaId = 'resources.meta.primaryKey';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	attributes: QMetaPrimaryKey.QAttributes = new QMetaPrimaryKey.QAttributes(this, 'attributes');
	relationships: QMetaKey.QRelationships = new QMetaKey.QRelationships(this, 'relationships');
}
export module QMetaPrimaryKey {
	export class QAttributes extends BeanPath<MetaPrimaryKey.Attributes> {
		generated: BooleanPath = this.createBoolean('generated');
		unique: BooleanPath = this.createBoolean('unique');
		name: StringPath = this.createString('name');
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