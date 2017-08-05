import {BeanPath, StringExpression} from '../expression/'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/'
import {MetaAttribute, QMetaAttribute} from './meta.attribute'
import {MetaDataObject, QMetaDataObject} from './meta.data.object'
import {MetaElement, QMetaElement} from './meta.element'
import {MetaInterface, QMetaInterface} from './meta.interface'
import {MetaKey, QMetaKey} from './meta.key'
import {MetaPrimaryKey, QMetaPrimaryKey} from './meta.primary.key'
import {MetaResourceBase} from './meta.resource.base'
import {MetaType, QMetaType} from './meta.type'
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module MetaResource {
	export interface Relationships extends MetaResourceBase.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaResourceBase.Attributes {
		resourceType?: string;
	}
}
export interface MetaResource extends MetaResourceBase {
	relationships?: MetaResource.Relationships;
	attributes?: MetaResource.Attributes;
}
export interface MetaResourceResult extends OneQueryResult {
	data?: MetaResource;
}
export interface MetaResourceListResult extends ManyQueryResult {
	data?: Array<MetaResource>;
}
export class QMetaResource extends BeanPath<MetaResource> {
	metaId: string = 'io.crnk.meta.resource.MetaResource';
	relationships: QMetaResource.QRelationships = new QMetaResource.QRelationships(this, 'relationships');
	attributes: QMetaResource.QAttributes = new QMetaResource.QAttributes(this, 'attributes');
}
export module QMetaResource {
	export class QRelationships extends BeanPath<MetaResource.Relationships> {
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
	export class QAttributes extends BeanPath<MetaResource.Attributes> {
		resourceType: StringExpression = this.createString('resourceType');
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaResource = function(id: string): MetaResource {
	return {
		id: id,
		type: 'meta/resource',
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