import {BeanPath, StringExpression} from '../expression/'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/'
import {MetaAttribute, QMetaAttribute} from './meta.attribute'
import {MetaDataObject, QMetaDataObject} from './meta.data.object'
import {MetaElement, QMetaElement} from './meta.element'
import {MetaInterface, QMetaInterface} from './meta.interface'
import {MetaKey, QMetaKey} from './meta.key'
import {MetaPrimaryKey, QMetaPrimaryKey} from './meta.primary.key'
import {MetaType, QMetaType} from './meta.type'
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module MetaJsonObject {
	export interface Relationships extends MetaDataObject.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaDataObject.Attributes {
	}
}
export interface MetaJsonObject extends MetaDataObject {
	relationships?: MetaJsonObject.Relationships;
	attributes?: MetaJsonObject.Attributes;
}
export interface MetaJsonObjectResult extends OneQueryResult {
	data?: MetaJsonObject;
}
export interface MetaJsonObjectListResult extends ManyQueryResult {
	data?: Array<MetaJsonObject>;
}
export class QMetaJsonObject extends BeanPath<MetaJsonObject> {
	metaId: string = 'io.crnk.meta.resource.MetaJsonObject';
	relationships: QMetaJsonObject.QRelationships = new QMetaJsonObject.QRelationships(this, 'relationships');
	attributes: QMetaJsonObject.QAttributes = new QMetaJsonObject.QAttributes(this, 'attributes');
}
export module QMetaJsonObject {
	export class QRelationships extends BeanPath<MetaJsonObject.Relationships> {
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
	export class QAttributes extends BeanPath<MetaJsonObject.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaJsonObject = function(id: string): MetaJsonObject {
	return {
		id: id,
		type: 'meta/jsonObject',
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