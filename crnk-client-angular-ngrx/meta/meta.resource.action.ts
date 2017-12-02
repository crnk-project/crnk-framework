import {
	BeanPath,
	StringPath
} from '../expression/';
import {
	MetaElement,
	QMetaElement
} from './meta.element';
import {MetaRepositoryActionType} from './meta.repository.action.type';
import {
	ManyQueryResult,
	OneQueryResult
} from 'ngrx-json-api';

export module MetaResourceAction {
	export interface Attributes extends MetaElement.Attributes {
		actionType?: MetaRepositoryActionType;
	}
}
export interface MetaResourceAction extends MetaElement {
	attributes?: MetaResourceAction.Attributes;
}
export interface MetaResourceActionResult extends OneQueryResult {
	data?: MetaResourceAction;
}
export interface MetaResourceActionListResult extends ManyQueryResult {
	data?: Array<MetaResourceAction>;
}
export class QMetaResourceAction extends BeanPath<MetaResourceAction> {
	metaId = 'resources.meta.resourceAction';
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
	attributes: QMetaResourceAction.QAttributes = new QMetaResourceAction.QAttributes(this, 'attributes');
	relationships: QMetaElement.QRelationships = new QMetaElement.QRelationships(this, 'relationships');
}
export module QMetaResourceAction {
	export class QAttributes extends BeanPath<MetaResourceAction.Attributes> {
		actionType: StringPath = this.createString('actionType');
		name: StringPath = this.createString('name');
	}
}
export let createEmptyMetaResourceAction = function(id: string): MetaResourceAction {
	return {
		id: id,
		type: 'meta/resourceAction',
		attributes: {
		},
	};
};