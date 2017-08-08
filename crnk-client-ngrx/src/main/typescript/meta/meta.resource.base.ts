import {BeanPath, StringExpression} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaAttribute, QMetaAttribute} from './meta.attribute';
import {MetaDataObject, QMetaDataObject} from './meta.data.object';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaInterface, QMetaInterface} from './meta.interface';
import {MetaJsonObject} from './meta.json.object';
import {MetaKey, QMetaKey} from './meta.key';
import {MetaPrimaryKey, QMetaPrimaryKey} from './meta.primary.key';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaResourceBase {
	export interface Relationships extends MetaJsonObject.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaJsonObject.Attributes {
	}
}
export interface MetaResourceBase extends MetaJsonObject {
	relationships?: MetaResourceBase.Relationships;
	attributes?: MetaResourceBase.Attributes;
}
export interface MetaResourceBaseResult extends OneQueryResult {
	data?: MetaResourceBase;
}
export interface MetaResourceBaseListResult extends ManyQueryResult {
	data?: Array<MetaResourceBase>;
}
export class QMetaResourceBase extends BeanPath<MetaResourceBase> {
	metaId = 'io.crnk.meta.resource.MetaResourceBase';
	relationships: QMetaResourceBase.QRelationships = new QMetaResourceBase.QRelationships(this, 'relationships');
	attributes: QMetaResourceBase.QAttributes = new QMetaResourceBase.QAttributes(this, 'attributes');
}
export module QMetaResourceBase {
	export class QRelationships extends BeanPath<MetaResourceBase.Relationships> {
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
	export class QAttributes extends BeanPath<MetaResourceBase.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaResourceBase = function(id: string): MetaResourceBase {
	return {
		id: id,
		type: 'meta/resourceBase',
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