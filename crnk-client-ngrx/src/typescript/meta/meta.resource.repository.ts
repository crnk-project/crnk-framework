import {BeanPath, StringExpression} from '../expression/'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/'
import {MetaDataObject, QMetaDataObject} from './meta.data.object'
import {MetaElement, QMetaElement} from './meta.element'
import {MetaResource, QMetaResource} from './meta.resource'
import {ManyQueryResult, OneQueryResult, ResourceRelationship, TypedOneResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module MetaResourceRepository {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
		resourceType?: TypedOneResourceRelationship<MetaResource>;
		listMetaType?: TypedOneResourceRelationship<MetaDataObject>;
		listLinksType?: TypedOneResourceRelationship<MetaDataObject>;
	}
	export interface Attributes extends MetaElement.Attributes {
	}
}
export interface MetaResourceRepository extends MetaElement {
	relationships?: MetaResourceRepository.Relationships;
	attributes?: MetaResourceRepository.Attributes;
}
export interface MetaResourceRepositoryResult extends OneQueryResult {
	data?: MetaResourceRepository;
}
export interface MetaResourceRepositoryListResult extends ManyQueryResult {
	data?: Array<MetaResourceRepository>;
}
export class QMetaResourceRepository extends BeanPath<MetaResourceRepository> {
	metaId: string = 'io.crnk.meta.resource.MetaResourceRepository';
	relationships: QMetaResourceRepository.QRelationships = new QMetaResourceRepository.QRelationships(this, 'relationships');
	attributes: QMetaResourceRepository.QAttributes = new QMetaResourceRepository.QAttributes(this, 'attributes');
}
export module QMetaResourceRepository {
	export class QRelationships extends BeanPath<MetaResourceRepository.Relationships> {
		resourceType: QTypedOneResourceRelationship<QMetaResource, MetaResource> = new QTypedOneResourceRelationship<QMetaResource, MetaResource>(this, 'resourceType', QMetaResource);
		listMetaType: QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject> = new QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'listMetaType', QMetaDataObject);
		listLinksType: QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject> = new QTypedOneResourceRelationship<QMetaDataObject, MetaDataObject>(this, 'listLinksType', QMetaDataObject);
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaResourceRepository.Attributes> {
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaResourceRepository = function(id: string): MetaResourceRepository {
	return {
		id: id,
		type: 'meta/resourceRepository',
		attributes: {
		},
		relationships: {
			resourceType: {data: null},
			listMetaType: {data: null},
			listLinksType: {data: null},
			parent: {data: null},
			children: {data: []},
		},
	};
};