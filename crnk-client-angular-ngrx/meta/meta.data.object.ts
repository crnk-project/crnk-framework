import {BeanPath, BooleanPath, StringPath} from '../expression/';
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
	export interface Attributes extends MetaElement.Attributes {
		insertable?: boolean;
		updatable?: boolean;
		deletable?: boolean;
		readable?: boolean;
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
	metaId = 'resources.meta.dataObject';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaDataObject.QRelationships = new QMetaDataObject.QRelationships(this, 'relationships');
	attributes: QMetaDataObject.QAttributes = new QMetaDataObject.QAttributes(this, 'attributes');
}
export module QMetaDataObject {
	export class QRelationships extends BeanPath<MetaDataObject.Relationships> {
		private _subTypes: QTypedManyResourceRelationship<QMetaDataObject, MetaDataObject>;
		get subTypes(): QTypedManyResourceRelationship<QMetaDataObject, MetaDataObject> {
			if(!this._subTypes){this._subTypes= new QTypedManyResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'subTypes', QMetaDataObject);}
			return this._subTypes
		};
		private _superType: QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>;
		get superType(): QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject> {
			if(!this._superType){this._superType= new QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'superType', QMetaDataObject);}
			return this._superType
		};
		private _attributes: QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>;
		get attributes(): QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute> {
			if(!this._attributes){this._attributes= new QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'attributes', QMetaAttribute);}
			return this._attributes
		};
		private _declaredAttributes: QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>;
		get declaredAttributes(): QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute> {
			if(!this._declaredAttributes){this._declaredAttributes= new QTypedManyResourceRelationship<QMetaAttribute, MetaAttribute>(this, 'declaredAttributes', QMetaAttribute);}
			return this._declaredAttributes
		};
		private _primaryKey: QTypedOneResourceRelationship<QMetaPrimaryKey, MetaPrimaryKey>;
		get primaryKey(): QTypedOneResourceRelationship<QMetaPrimaryKey, MetaPrimaryKey> {
			if(!this._primaryKey){this._primaryKey= new QTypedOneResourceRelationship<QMetaPrimaryKey, MetaPrimaryKey>(this, 'primaryKey', QMetaPrimaryKey);}
			return this._primaryKey
		};
		private _declaredKeys: QTypedManyResourceRelationship<QMetaKey, MetaKey>;
		get declaredKeys(): QTypedManyResourceRelationship<QMetaKey, MetaKey> {
			if(!this._declaredKeys){this._declaredKeys= new QTypedManyResourceRelationship<QMetaKey, MetaKey>(this, 'declaredKeys', QMetaKey);}
			return this._declaredKeys
		};
		private _interfaces: QTypedManyResourceRelationship<QMetaInterface, MetaInterface>;
		get interfaces(): QTypedManyResourceRelationship<QMetaInterface, MetaInterface> {
			if(!this._interfaces){this._interfaces= new QTypedManyResourceRelationship<QMetaInterface, MetaInterface>(this, 'interfaces', QMetaInterface);}
			return this._interfaces
		};
		private _elementType: QTypedOneResourceRelationship<QMetaType, MetaType>;
		get elementType(): QTypedOneResourceRelationship<QMetaType, MetaType> {
			if(!this._elementType){this._elementType= new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);}
			return this._elementType
		};
		private _parent: QTypedOneResourceRelationship<QMetaElement, MetaElement>;
		get parent(): QTypedOneResourceRelationship<QMetaElement, MetaElement> {
			if(!this._parent){this._parent= new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);}
			return this._parent
		};
		private _children: QTypedManyResourceRelationship<QMetaElement, MetaElement>;
		get children(): QTypedManyResourceRelationship<QMetaElement, MetaElement> {
			if(!this._children){this._children= new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);}
			return this._children
		};
	}
	export class QAttributes extends BeanPath<MetaDataObject.Attributes> {
		insertable: BooleanPath = this.createBoolean('insertable');
		updatable: BooleanPath = this.createBoolean('updatable');
		deletable: BooleanPath = this.createBoolean('deletable');
		readable: BooleanPath = this.createBoolean('readable');
		name: StringPath = this.createString('name');
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