import {BeanPath, StringExpression} from '../expression/'
import {QTypedManyResourceRelationship, QTypedOneResourceRelationship} from '../stub/'
import {MetaElement, QMetaElement} from './meta.element'
import {MetaRepositoryActionType} from './meta.repository.action.type'
import {ManyQueryResult, OneQueryResult, ResourceRelationship} from 'ngrx-json-api/src/interfaces'

export module MetaResourceAction {
	export interface Relationships extends MetaElement.Relationships {
		[key: string]: ResourceRelationship;
	}
	export interface Attributes extends MetaElement.Attributes {
		actionType?: MetaRepositoryActionType;
	}
}
export interface MetaResourceAction extends MetaElement {
	relationships?: MetaResourceAction.Relationships;
	attributes?: MetaResourceAction.Attributes;
}
export interface MetaResourceActionResult extends OneQueryResult {
	data?: MetaResourceAction;
}
export interface MetaResourceActionListResult extends ManyQueryResult {
	data?: Array<MetaResourceAction>;
}
export class QMetaResourceAction extends BeanPath<MetaResourceAction> {
	metaId: string = 'io.crnk.meta.resource.MetaResourceAction';
	relationships: QMetaResourceAction.QRelationships = new QMetaResourceAction.QRelationships(this, 'relationships');
	attributes: QMetaResourceAction.QAttributes = new QMetaResourceAction.QAttributes(this, 'attributes');
}
export module QMetaResourceAction {
	export class QRelationships extends BeanPath<MetaResourceAction.Relationships> {
		parent: QTypedOneResourceRelationship<QMetaElement, MetaElement> = new QTypedOneResourceRelationship<QMetaElement, MetaElement>(this, 'parent', QMetaElement);
		children: QTypedManyResourceRelationship<QMetaElement, MetaElement> = new QTypedManyResourceRelationship<QMetaElement, MetaElement>(this, 'children', QMetaElement);
	}
	export class QAttributes extends BeanPath<MetaResourceAction.Attributes> {
		actionType: StringExpression = this.createString('actionType');
		name: StringExpression = this.createString('name');
	}
}
export let createEmptyMetaResourceAction = function(id: string): MetaResourceAction {
	return {
		id: id,
		type: 'meta/resourceAction',
		attributes: {
		},
		relationships: {
			parent: {data: null},
			children: {data: []},
		},
	};
};