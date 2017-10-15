import {BeanPath, StringPath} from '../expression/';
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/';
import {MetaDataObject, QMetaDataObject} from './meta.data.object';
import {MetaElement, QMetaElement} from './meta.element';
import {MetaResource, QMetaResource} from './meta.resource';
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces';

export module MetaResourceRepository {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
		resourceType?: TypedOneResourceRelationship<MetaResource>;
		listMetaType?: TypedOneResourceRelationship<MetaDataObject>;
		listLinksType?: TypedOneResourceRelationship<MetaDataObject>;
	}
}
export interface MetaResourceRepository extends MetaElement {
	relationships?: MetaResourceRepository.Relationships;
}
export interface MetaResourceRepositoryResult extends OneQueryResult {
	data?: MetaResourceRepository;
}
export interface MetaResourceRepositoryListResult extends ManyQueryResult {
	data?: Array<MetaResourceRepository>;
}
export class QMetaResourceRepository extends BeanPath<MetaResourceRepository> {
	metaId = 'resources.meta.resourceRepository';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	relationships: QMetaResourceRepository.QRelationships = new QMetaResourceRepository.QRelationships(this, 'relationships');
	attributes: QMetaElement.QAttributes = new QMetaElement.QAttributes(this, 'attributes');
}
export module QMetaResourceRepository {
	export class QRelationships extends BeanPath<MetaResourceRepository.Relationships> {
		private _resourceType: QTypedOneResourceRelationship<QMetaResource, MetaResource>;
		get resourceType(): QTypedOneResourceRelationship<QMetaResource, MetaResource> {
			if(!this._resourceType){this._resourceType= new QTypedOneResourceRelationship<QMetaResource, MetaResource>(this, 'resourceType', QMetaResource);}
			return this._resourceType
		};
		private _listMetaType: QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>;
		get listMetaType(): QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject> {
			if(!this._listMetaType){this._listMetaType= new QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'listMetaType', QMetaDataObject);}
			return this._listMetaType
		};
		private _listLinksType: QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>;
		get listLinksType(): QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject> {
			if(!this._listLinksType){this._listLinksType= new QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'listLinksType', QMetaDataObject);}
			return this._listLinksType
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
export let createEmptyMetaResourceRepository = function(id: string): MetaResourceRepository {
	return {
		id: id,
		type: 'meta/resourceRepository',
		relationships: {
			resourceType: {data: null},
			listMetaType: {data: null},
			listLinksType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};