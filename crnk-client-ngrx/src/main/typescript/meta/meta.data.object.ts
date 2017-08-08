import {BeanPath, StringExpression} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaAttribute, QMetaAttribute} from './meta.attribute';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaInterface, QMetaInterface} from './meta.interface';
import {MetaKey, QMetaKey} from './meta.key';
import {MetaPrimaryKey, QMetaPrimaryKey} from './meta.primary.key';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedManyResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaDataObject {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
		subTypes?: TypedManyResourceRelationship<MetaDataObject>;
		superType?: TypedOneResourceRelationship<MetaDataObject>;
		attributes?: TypedManyResourceRelationship<MetaAttribute>;
		declaredAttributes?: TypedManyResourceRelationship<MetaAttribute>;
		primaryKey?: TypedOneResourceRelationship<MetaPrimaryKey>;
		declaredKeys?: TypedManyResourceRelationship<MetaKey>;
		interfaces?: TypedManyResourceRelationship<MetaInterface>;
	}
	export interface Attributes extends MetaType.Attributes {
	}
}
export interface MetaDataObject extends MetaType {
	relationships?: MetaDataObject.Relationships;
	attributes?: MetaDataObject.Attributes;
}
export interface MetaDataObjectResult extends OneQueryResult {
	data?: MetaDataObject;
}
export interface MetaDataObjectListResult extends ManyQueryResult {
	data?: Array<MetaDataObject>;
}
export class QMetaDataObject extends BeanPath<MetaDataObject> {
	metaId = 'io.crnk.meta.MetaDataObject';
	relationships: QMetaDataObject.QRelationships = new QMetaDataObject.QRelationships(this, 'relationships');
	attributes: QMetaDataObject.QAttributes = new QMetaDataObject.QAttributes(this, 'attributes');
}
export module QMetaDataObject {
	export class QRelationships extends BeanPath<MetaDataObject.Relationships> {
		subTypes: QTypedManyResourceRelationship<QMetaDataObject, MetaDataObject> = new QTypedManyResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'subTypes', QMetaDataObject);
		superType: QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject> = new QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'superType', QMetaDataObject);
		attributes: QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute> = new QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'attributes', QMetaAttribute);
		declaredAttributes: QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute> = new QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'declaredAttributes', QMetaAttribute);
		primaryKey: QTypedOneResourceRelationship<QMetaPrimaryKey, MetaPrimaryKey> = new QTypedOneResourceRelationship<QMetaPrimaryKey, MetaPrimaryKey>(this, 'primaryKey', QMetaPrimaryKey);
		declaredKeys: QTypedManyResourceRelationship<QMetaKey, MetaKey> = new QTypedManyResourceRelationship<QMetaKey, MetaKey>(this, 'declaredKeys', QMetaKey);
		interfaces: QTypedManyResourceRelationship<QMetaInterface, MetaInterface> = new QTypedManyResourceRelationship<QMetaInterface, MetaInterface>(this, 'interfaces', QMetaInterface);
		elementType: QTypedOneResourceRelationship<QMetaType, MetaType> = new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaDataObject.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaDataObject = function(id: string): MetaDataObject {
	return {
		id: id,
		type: 'meta/dataObject',
		attributes: {
		},
		relationships: {
			subTypes: {data: []},
			superType: {data: null},
			attributes: {data: []},
			declaredAttributes: {data: []},
			primaryKey: {data: null},
			declaredKeys: {data: []},
			interfaces: {data: []},
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};