import {BeanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaType, QMetaType} from './meta.type';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaMapType {
	export interface Relationships extends MetaType.Relationships {
		[key: string]: ResourceRelationship;
		keyType?: TypedOneResourceRelationship<MetaType>;
	}
}
export interface MetaMapType extends MetaType {
	relationships?: MetaMapType.Relationships;
}
export interface MetaMapTypeResult extends OneQueryResult {
	data?: MetaMapType;
}
export interface MetaMapTypeListResult extends ManyQueryResult {
	data?: Array<MetaMapType>;
}
export class QMetaMapType extends BeanPath<MetaMapType> {
	metaId = 'resources.meta.mapType';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaMapType.QRelationships = new QMetaMapType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaMapType {
	export class QRelationships extends BeanPath<MetaMapType.Relationships> {
		private _keyType: QTypedOneResourceRelationship<QMetaType, MetaType>;
		get keyType(): QTypedOneResourceRelationship<QMetaType, MetaType> {
			if(!this._keyType){this._keyType= new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'keyType', QMetaType);}
			return this._keyType
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
}
export let createEmptyMetaMapType = function(id: string): MetaMapType {
	return {
		id: id,
		type: 'meta/mapType',
		relationships: {
			keyType: {data: null},
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};