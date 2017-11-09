import {
	BeanPath,
	StringPath
} from '../expression/';
import {
	CrnkStoreResource,
	QTypedManyResourceRelationship,
	QTypedOneResourceRelationship
} from '../stub/';
import {
	ManyQueryResult,
	OneQueryResult,
	ResourceRelationship,
	TypedManyResourceRelationship,
	TypedOneResourceRelationship
} from 'ngrx-json-api/src/interfaces';

export module MetaElement {
	export interface Relationships {
		[key: string]: ResourceRelationship;
		parent?: TypedOneResourceRelationship<MetaElement>;
		children?: TypedManyResourceRelationship<MetaElement>;
	}
	export interface Attributes {
		name?: string;
	}
}
export interface MetaElement extends CrnkStoreResource {
	relationships?: MetaElement.Relationships;
	attributes?: MetaElement.Attributes;
}
export interface MetaElementResult extends OneQueryResult {
	data?: MetaElement;
}
export interface MetaElementListResult extends ManyQueryResult {
	data?: Array<MetaElement>;
}
export class QMetaElement extends BeanPath<MetaElement> {
	metaId = 'resources.meta.element';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaElement.QRelationships = new QMetaElement.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaElement {
	export class QRelationships extends BeanPath<MetaElement.Relationships> {
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
	export class QAttributes extends BeanPath<MetaElement.Attributes> {
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaElement = function(id: string): MetaElement {
	return {
		id: id,
		type: 'meta/element',
		attributes: {
		},
		relationships: {
			parent: {data: null},
			children: {data: []},
		},
	};
};