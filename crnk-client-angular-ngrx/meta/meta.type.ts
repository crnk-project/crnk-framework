import {
	BeanPath,
	StringPath
} from '../expression/';
import {
	QTypedManyResourceRelationship,
	QTypedOneResourceRelationship
} from '../stub/';
import {
	MetaElement,
	QMetaElement
} from './meta.element';
import {
	ManyQueryResult,
	OneQueryResult,
	ResourceRelationship,
	TypedOneResourceRelationship
} from 'ngrx-json-api';

export module MetaType {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
		elementType?: TypedOneResourceRelationship<MetaType>;
	}
}
export interface MetaType extends MetaElement {
	relationships?: MetaType.Relationships;
}
export interface MetaTypeResult extends OneQueryResult {
	data?: MetaType;
}
export interface MetaTypeListResult extends ManyQueryResult {
	data?: Array<MetaType>;
}
export class QMetaType extends BeanPath<MetaType> {
	metaId = 'resources.meta.type';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaType.QRelationships = new QMetaType.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaType {
	export class QRelationships extends BeanPath<MetaType.Relationships> {
		private _elementType: QTypedOneResourceRelationship<QMetaType, MetaType>;
		get elementType(): QTypedOneResourceRelationship<QMetaType, MetaType> {
			if (!this._elementType) {
				this._elementType =
					new QTypedOneResourceRelationship<QMetaType, MetaType>(this, 'elementType', QMetaType);
			}
			return this._elementType;
		};
		private _parent: QTypedOneResourceRelationship<QMetaElement, MetaElement>;
		get parent(): QTypedOneResourceRelationship<QMetaElement, MetaElement> {
			if (!this._parent) {
				this._parent =
					new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
			}
			return this._parent;
		};
		private _children: QTypedManyResourceRelationship<QMetaElement, MetaElement>;
		get children(): QTypedManyResourceRelationship<QMetaElement, MetaElement> {
			if (!this._children) {
				this._children =
					new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
			}
			return this._children;
		};
	}
}
export let createEmptyMetaType = function(id: string): MetaType {
	return {
		id: id,
		type: 'meta/type',
		relationships: {
			elementType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};