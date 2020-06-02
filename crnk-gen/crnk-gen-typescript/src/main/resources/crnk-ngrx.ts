import {OperationType, Resource, ResourceError, ResourceState, ManyResourceRelationship, OneResourceRelationship, ResourceIdentifier, StoreResource} from 'ngrx-json-api';
import * as _ from 'lodash';

export const toQueryPath = function (attributePath: string | Expression<any>): string {
	const strAttributePath = attributePath.toString();

	const pathElements = strAttributePath.split('.');

	const searchPath = [];

	for (let i = 0; i < pathElements.length; i++) {
		if (pathElements[i] === 'attributes') {
			// nothing to do
		}
		else if (pathElements[i] === 'relationships' && i < pathElements.length - 2) {
			const relationshipName = pathElements[i + 1];
			const dataType = pathElements[i + 2];
			if (dataType === 'data' || dataType === 'reference') {
				searchPath.push(relationshipName);
				i += 2;
			}
			else {
				throw new Error('cannot map relationship path in ' + attributePath + ', got ' + dataType +
					' but expected data or reference');
			}
		}
		else {
			searchPath.push(pathElements[i]);
		}
	}
	return _.join(searchPath, '.');
};

export interface Expression<T> {

	/**
	 * @return true if the given object is a literal expression.
	 */
	isConstant(): boolean;

	/**
	 * @return true if the given object is a path expression.
	 */
	isPath(): boolean;

}

export interface ExpressionAccessor {

	setExpression(expression: Expression<any>);
}


/**
 * Refers to a property of an object.
 */
export interface Path<T> extends Expression<T> {

	/**
	 * @return string representation of this expression.
	 */
	toString(): string;

	/**
	 * @return the value of this path if it is bound to an data object (see {@link BeanBinding}.
	 */
	getValue(): any;

	/**
	 * Sets a new value for the given path.
	 *
	 * @param newValue
	 */
	setValue(value: any);

	/**
	 * @return name to use for forFormElement elements to uniquely identity this path. This current corresponds
	 * to toString, will later be extended to include resource$ identifiers to support complex forFormElement with
	 * multiple editors (TODO).
	 */
	toFormName(): string;

	/**
	 * Underlying resource holding the values.
	 *
	 * @returns {any}
	 */
	getResource(): any;

	/**
	 * Pointer of this path from the resource.
	 */
	getSourcePointer(): String;

	/**
	 * @returns {string} path used for sorting and filtering (excludes any 'relationships' and 'attributes' in the path)
	 */
	toQueryPath(): string;
}

export const OPERATION_EQ = 'EQ';
export const OPERATION_NEQ = 'NEQ';
export const OPERATION_LIKE = 'LIKE';
export const OPERATION_GT = 'GT';
export const OPERATION_LT = 'LT';
export const OPERATION_GE = 'GE';
export const OPERATION_LE = 'LE';

/**
 * Base implementation for a {@link Expression} providing a EQ and NEQ operation.
 */
export class SimpleExpression<T> implements Expression<T> {

	public eq(right: T): BooleanExpression {
		return new BooleanOperation(OPERATION_EQ, this, ExpressionFactory.toLiteral(right));
	}

	public neq(right: T): BooleanExpression {
		return new BooleanOperation(OPERATION_NEQ, this, ExpressionFactory.toLiteral(right));
	}

	isConstant(): boolean {
		return false;
	}

	isPath(): boolean {
		return false;
	}
}

/**
 * Base implementation for a {@link Expression} providing equals and compare operations.
 */
export abstract class ComparableExpression<T> extends SimpleExpression<T> {

	public lt(right: T): BooleanExpression {
		return new BooleanOperation(OPERATION_LT.toString(), this, ExpressionFactory.toLiteral(right));
	}

	public le(right: T): BooleanExpression {
		return new BooleanOperation(OPERATION_LE.toString(), this, ExpressionFactory.toLiteral(right));
	}

	public gt(right: T): BooleanExpression {
		return new BooleanOperation(OPERATION_GT.toString(), this, ExpressionFactory.toLiteral(right));
	}

	public ge(right: T): BooleanExpression {
		return new BooleanOperation(OPERATION_GE.toString(), this, ExpressionFactory.toLiteral(right));
	}
}

/**
 * Base implementation for {@link Expression} bound to a string.
 */
export abstract class StringExpression extends ComparableExpression<string> {

	public like(right: string): BooleanExpression {
		return new BooleanOperation(OPERATION_LIKE.toString(), this, new StringConstant(right));
	}
}

/**
 * Base implementation for {@link Expression} bound to a number.
 */
export abstract class NumberExpression extends ComparableExpression<number> {
}

/**
 * Base implementation for {@link Expression} bound to a boolean.
 */
export abstract class BooleanExpression extends ComparableExpression<boolean> {

}

/**
 * Represents a boolean operation comparing two expressions with the provided operation.
 */
export class BooleanOperation extends BooleanExpression {

	public operation: string;
	public expressions: Expression<any>[];

	public constructor(operation: string, left: Expression<any>, right: Expression<any>) {
		super();
		this.operation = operation;
		this.expressions = [left, right];
	}
}

/**
 * Helper methods to create expressions.
 */
export class ExpressionFactory {

	public static toBeanPath<T>(object: any, attributeNames: Array<string>): BeanPath<any> {
		let path: BeanPath<any> = new BeanBinding(object);
		for (const attributeName of attributeNames) {
			path = new BeanPath(path, attributeName);
		}
		return path;
	}

	public static toLiteral<T>(value: any): Constant<T> {
		if (typeof value === 'string') {
			return new StringConstant(value as string) as Constant<any>;
		}
		else if (typeof value === 'number') {
			return new NumberConstant(value as number) as Constant<any>;
		}
		else if (typeof value === 'boolean') {
			return new BooleanConstant(value as boolean) as Constant<any>;
		}
		else {
			throw Error('unknown type for value ' + value);
		}
	}

	static concat(parent: Expression<any>, value1: string) {
		const value0 = parent != null ? parent.toString() : null;
		if (value0) {
			if (parent instanceof MapPath || parent instanceof ArrayPath) {
				return value0 + '[' + value1 + ']';
			} else {
				return value0 + '.' + value1;
			}
		}
		if (value1) {
			return value1;
		}
		return null;
	}
}

/**
 * Represents a constant expression.
 */
export interface Constant<T> extends Expression<T> {
	value: T;
}

export class BooleanConstant extends BooleanExpression implements Constant<boolean> {
	value: boolean;

	constructor(value: boolean) {
		super();
		this.value = value;
	}

	isConstant(): boolean {
		return true;
	}

	toString(): string {
		return this.value.toString();
	}
}


function toSourcePointerInternal(path: string, resource: any) {
	if (resource !== null) {
		return '/data/' + path.replace(new RegExp('\\.', 'g'), '/');
	}
	else {
		throw new Error('resource not available for ' + path.toString());
	}
}

function toFormNameInternal(path: Path<any>, resource: any) {
	if (resource !== null) {
		return '//' + resource.type + '//' + resource.id + '//' + path.toString();
	}
	else {
		throw new Error('resource not available for ' + path.toString());
	}
}

export class NumberConstant extends NumberExpression implements Constant<number> {
	value: number;

	constructor(literal: number) {
		super();
		this.value = literal;
	}

	isConstant(): boolean {
		return true;
	}

	toString(): string {
		return this.value.toString();
	}
}

export class StringConstant extends StringExpression implements Constant<string> {
	value: string;

	constructor(value: string) {
		super();
		this.value = value;
	}

	isConstant(): boolean {
		return true;
	}

	toString(): string {
		return this.value;
	}
}

/**
 * Represents a path accessing a object property.
 */
export class BeanPath<T> extends SimpleExpression<T> implements Path<T> {


	constructor(public parentPath: BeanPath<any>, private property?: string) {
		super();
	}

	protected add<P extends Path<any>>(path: P): P {
		return path;
	}

	protected createString(property: string): StringPath {
		return this.add(new StringPath(this, property));
	}

	protected createBoolean(property: string): BooleanPath {
		return this.add(new BooleanPath(this, property));
	}

	protected createNumber(property: string): NumberPath {
		return this.add(new NumberPath(this, property));
	}

	toString(): string {
		if (this.parentPath) {
			if (this.parentPath !== null) {
				return ExpressionFactory.concat(this.parentPath, this.property);
			}
		}
		return this.property;
	}

	isPath(): boolean {
		return true;
	}

	getValue() {
		return doGetValue(this.parentPath, this.property);
	}

	setValue(newValue) {
		return doSetValue(this.parentPath, this.property, newValue);
	}

	getResource() {
		return this.parentPath.getResource();
	}

	toFormName(): string {
		return toFormNameInternal(this, this.parentPath.getResource());
	}

	getSourcePointer(): string {
		return toSourcePointerInternal(this.toString(), this.parentPath.getResource());
	}

	toQueryPath(): string {
		return toQueryPath(this.toString());
	}
}


/**
 * Represents a path accessing a string property.
 */
export class StringPath extends StringExpression implements Path<string> {

	private parent: BeanPath<any>;
	private property: string;

	constructor(parent: BeanPath<any>, property: string) {
		super();
		this.parent = parent;
		this.property = property;
	}

	toString(): string {
		return ExpressionFactory.concat(this.parent, this.property);
	}

	isPath(): boolean {
		return true;
	}

	getValue() {
		return doGetValue(this.parent, this.property);
	}

	setValue(newValue) {
		return doSetValue(this.parent, this.property, newValue);
	}

	toFormName(): string {
		return toFormNameInternal(this, this.parent.getResource());
	}

	getResource() {
		return this.parent.getResource();
	}

	getSourcePointer(): string {
		return toSourcePointerInternal(this.toString(), this.parent.getResource());
	}

	toQueryPath(): string {
		return toQueryPath(this.toString());
	}
}


export class ArrayPath<Q extends Expression<T>, T> extends SimpleExpression<Array<T>> implements Path<Array<T>> {

	constructor(private parent: BeanPath<any>, private property: string, private _referenceType: new (...args: any[]) => Q) {
		super();
	}

	public getElement(index: number): Q {
		return new this._referenceType(this, index);
	}

	toString(): string {
		return ExpressionFactory.concat(this.parent, this.property);
	}

	isPath(): boolean {
		return true;
	}

	isConstant(): boolean {
		return false;
	}

	getValue() {
		return doGetValue(this.parent, this.property);
	}

	setValue(newValue) {
		return doSetValue(this.parent, this.property, newValue);
	}

	toFormName(): string {
		return toFormNameInternal(this as Path<any>, this.parent.getResource());
	}

	getResource() {
		return this.parent.getResource();
	}

	getSourcePointer(): string {
		return toSourcePointerInternal(this.toString(), this.parent.getResource());
	}

	toQueryPath(): string {
		return toQueryPath(this.toString());
	}
}

export class MapPath<Q extends Expression<T>, T> extends SimpleExpression<Array<T>> implements Path<Array<T>> {

	constructor(private parent: BeanPath<any>, private property: string, private _referenceType: new (...args: any[]) => Q) {
		super();
	}

	public getElement(key: any): Q {
		return new this._referenceType(this, key);
	}

	toString(): string {
		return ExpressionFactory.concat(this.parent, this.property);
	}

	isPath(): boolean {
		return true;
	}

	isConstant(): boolean {
		return false;
	}

	getValue() {
		return doGetValue(this.parent, this.property);
	}

	setValue(newValue) {
		return doSetValue(this.parent, this.property, newValue);
	}

	toFormName(): string {
		return toFormNameInternal(this as Path<any>, this.parent.getResource());
	}

	getResource() {
		return this.parent.getResource();
	}

	getSourcePointer(): string {
		return toSourcePointerInternal(this.toString(), this.parent.getResource());
	}

	toQueryPath(): string {
		return toQueryPath(this.toString());
	}
}


function doGetValue(parent: Path<any>, property: string) {
	const parentValue = parent.getValue();

	// consider to eliminate this case, root between binding and path
	if (!property) {
		return parentValue;
	}
	if (parentValue != null) {
		return parentValue[property];
	}
	else {
		return null;
	}
}

function doSetValue(parent: Path<any>, property: string, newValue) {
	const parentValue = parent.getValue();
	if (parentValue === null) {
		throw new Error(); // TODO setup map in parent
	}

	if (!property) {
		return new Error();
	}
	parentValue[property] = newValue;
}

/**
 * Represents a path accessing a number property.
 */
export class NumberPath extends NumberExpression implements Path<number> {

	private parent: BeanPath<any>;
	private property: string;

	constructor(parent: BeanPath<any>, property: string) {
		super();
		this.parent = parent;
		this.property = property;
	}

	toString(): string {
		return ExpressionFactory.concat(this.parent, this.property);
	}

	isPath(): boolean {
		return true;
	}

	getValue() {
		return doGetValue(this.parent, this.property);
	}

	setValue(newValue) {
		return doSetValue(this.parent, this.property, newValue);
	}

	toFormName(): string {
		return toFormNameInternal(this, this.parent.getResource());
	}

	getResource() {
		return this.parent.getResource();
	}

	getSourcePointer(): string {
		return toSourcePointerInternal(this.toString(), this.parent.getResource());
	}

	toQueryPath(): string {
		return toQueryPath(this.toString());
	}
}


/**
 * Represents a path accessing a boolean property.
 */
export class BooleanPath extends BooleanExpression implements Path<boolean> {

	private parent: BeanPath<any>;
	private property: string;

	constructor(parent: BeanPath<any>, property: string) {
		super();
		this.parent = parent;
		this.property = property;
	}

	toString(): string {
		return ExpressionFactory.concat(this.parent, this.property);
	}

	isPath(): boolean {
		return true;
	}

	getValue() {
		return doGetValue(this.parent, this.property);
	}

	setValue(newValue) {
		return doSetValue(this.parent, this.property, newValue);
	}

	toFormName(): string {
		return toFormNameInternal(this, this.parent.getResource());
	}

	getResource() {
		return this.parent.getResource();
	}

	getSourcePointer(): string {
		return toSourcePointerInternal(this.toString(), this.parent.getResource());
	}

	toQueryPath(): string {
		return toQueryPath(this.toString());
	}
}

/**
 * Represents a direct refresh to object. Sits at the root of an expression chain.
 */
export class BeanBinding extends BeanPath<any> {

	constructor(public bean: any) {
		super(null, null);
	}

	toString(): string {
		return null;
	}

	getValue() {
		return this.bean;
	}

	setValue(value: any) {
		this.bean = value;
	}

	getResource() {
		return this.bean;
	}
}












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
	id: StringPath = this.createString('id');
	type: StringPath = this.createString('type');
}


export class QTypedOneResourceRelationship<Q extends BeanPath<T>, T extends StoreResource>  extends BeanPath<TypedOneResourceRelationship<T>> {
	data: QResourceIdentifier = new QResourceIdentifier(this, 'data');

	private _reference: Q;

	constructor(parent: BeanPath<any>, property: string, private _referenceType: new (...args: any[]) => Q) {
		super(parent, property);
	}

	public get reference(): Q {
		if (this._reference == null) {
			this._reference = new this._referenceType(null, 'reference');
			this._reference.parentPath = this;
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
			this._reference = new this._referenceType(null, 'reference');
			this._reference.parentPath = this;
		}
		return this._reference;
	}
}

