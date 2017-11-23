import {
	BeanPath,
	BooleanPath,
	StringPath
} from '../expression/';
import {
	MetaAttribute,
	QMetaAttribute
} from './meta.attribute';
import {
	ManyQueryResult,
	OneQueryResult
} from 'ngrx-json-api';

export module MetaResourceField {
	export interface Attributes extends MetaAttribute.Attributes {
		meta?: boolean;
		links?: boolean;
	}
}
export interface MetaResourceField extends MetaAttribute {
	attributes?: MetaResourceField.Attributes;
}
export interface MetaResourceFieldResult extends OneQueryResult {
	data?: MetaResourceField;
}
export interface MetaResourceFieldListResult extends ManyQueryResult {
	data?: Array<MetaResourceField>;
}
export class QMetaResourceField extends BeanPath<MetaResourceField> {
	metaId = 'resources.meta.resourceField';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	attributes: QMetaResourceField.QAttributes = new QMetaResourceField.QAttributes(this, 'attributes');
	relationships: QMetaAttribute.QRelationships = new QMetaAttribute.QRelationships(this, 'relationships');
}
export module QMetaResourceField {
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
		readable: BooleanPath = this.createBoolean('readable');
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaResourceField = function(id: string): MetaResourceField {
	return {
		id: id,
		type: 'meta/resourceField',
		attributes: {
		},
	};
};