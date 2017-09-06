import {OperationType, Resource, ResourceError, ResourceState, ManyResourceRelationship, OneResourceRelationship, ResourceIdentifier, StoreResource} from 'ngrx-json-api';
import {BeanPath, StringExpression} from '../expression';

/**
 * For some reason the compiler does not get in applicaiton projects it if we make use of StoreResource directly.
 * For this reason we repeat the fields here.
 */
export interface CrnkStoreResource extends StoreResource {
	type: string;
	id: string;
	state?: ResourceState;
	persistedResource?: Resource;
	loading?: OperationType;
	errors?: Array<ResourceError>;
	hasTemporaryId?: boolean;
}

export interface TypedManyResourceRelationship<T extends StoreResource> extends ManyResourceRelationship {
	reference?: Array<T>;
}

export interface TypedOneResourceRelationship<T extends StoreResource>  extends OneResourceRelationship {
	reference?: T;
}


export class QResourceIdentifier extends BeanPath<ResourceIdentifier> {
	id: StringExpression = this.createString('id');
	type: StringExpression = this.createString('type');
}


export class QTypedOneResourceRelationship<Q extends BeanPath<T>, T extends StoreResource>  extends BeanPath<TypedOneResourceRelationship<T>> {
	data: QResourceIdentifier = new QResourceIdentifier(this, 'data');

	private _reference: Q;

	constructor(parent: BeanPath<any>, property: string, private _referenceType: new (...args: any[]) => Q) {
		super(parent, property);
	}

	public get reference(): Q {
		if (this._reference == null) {
			this._reference = new this._referenceType(null, 'data');
			this._reference.parent = this;
		}
		return this._reference;
	}
}

export class QTypedManyResourceRelationship<Q extends BeanPath<T>, T extends StoreResource>  extends BeanPath<TypedManyResourceRelationship<T>> {
	data: QResourceIdentifier = new QResourceIdentifier(this, 'data');

	private _reference: Q;

	constructor(parent: BeanPath<any>, property: string, private _referenceType: new (...args: any[]) => Q) {
		super(parent, property);
	}

	public get reference(): Q {
		if (this._reference == null) {
			this._reference = new this._referenceType(null, 'data');
			this._reference.parent = this;
		}
		return this._reference;
	}
}

