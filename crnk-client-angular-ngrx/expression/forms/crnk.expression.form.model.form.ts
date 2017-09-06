import {Directive, EventEmitter, forwardRef, Inject, Input, OnChanges, OnDestroy, Optional, Output, Self} from '@angular/core';
import {
	AbstractFormGroupDirective,
	AsyncValidatorFn,
	ControlContainer,
	ControlValueAccessor,
	NG_ASYNC_VALIDATORS,
	NG_VALIDATORS,
	NG_VALUE_ACCESSOR,
	NgControl,
	NgForm,
	NgModelGroup,
	Validator,
	ValidatorFn
} from '@angular/forms';
import {controlPath, selectValueAccessor, TemplateDrivenErrors} from './crnk.expression.form.utils';
import {ExpressionAccessor, Path} from '../crnk.expression';
import {CrnkControl} from './crnk.expression.form.model.base';

const formExpressionBinding: any = {
	provide: NgControl,
	useExisting: forwardRef(() => FormExpressionDirective)
};


/**
 * Allows to bind expressions of type {link Expression} to expectForm controls. The directive is very similar to
 * NgModel, but allows to bind an expression instead of a generic object. The advantage is that an expression
 * can hold both
 */
@Directive({
	selector: '[crnkFormExpression]:not([formControlName]):not([formControl])',
	providers: [formExpressionBinding],
	exportAs: 'crnkFormExpression'
})
export class FormExpressionDirective extends CrnkControl implements OnChanges, OnDestroy {

	public get pathModel() {
		return this._pathModel;
	}


	@Input('crnkFormExpression')
	public set pathModel(pathModel: Path<any>) {
		this._pathModel = pathModel;
		this._control['_pathModel'] = pathModel;

		const expressionAccessor = this.valueAccessor as any as ExpressionAccessor;
		if (!expressionAccessor) {
			throw new Error('no value accessor found');
		}
		if (expressionAccessor.setExpression) {
			expressionAccessor.setExpression(pathModel);
		}
	}


	name: string;


	@Output('arbFormExpressionChange') arbFormExpressionChange = new EventEmitter();

	/**
	 * NOTE that in contract to NgModel we do not use @Host for parent. This would enforce that the
	 * components are direct children of the forms component. TODO investigate whether there is
	 * any better solutions (maybe something similar to FormGroup). It does not seem like it is possible
	 * to expose an existing ControlContainer again with @Component.providers as it would lead to a circular
	 * dependency (there is no provider for children only, but itself as well).
	 */
	constructor(
		@Optional() parent: ControlContainer,
		@Optional() @Self() @Inject(NG_VALIDATORS) validators: Array<Validator | ValidatorFn>,
		@Optional() @Self() @Inject(NG_ASYNC_VALIDATORS) asyncValidators: Array<Validator | AsyncValidatorFn>,
		@Optional() @Self() @Inject(NG_VALUE_ACCESSOR) valueAccessors: ControlValueAccessor[]
	) {
		super();
		this._parent = parent;
		this._rawValidators = validators || [];
		this._rawAsyncValidators = asyncValidators || [];
		this.valueAccessor = selectValueAccessor(this, valueAccessors);
	}


	ngOnDestroy(): void {
		if (this.formDirective) {
			this.formDirective.removeControl(this);
		}
	}

	get path(): string[] {
		if(!this._pathModel){
			return [];
		}
		if(this._parent){
			return controlPath(this._pathModel.toFormName(), this._parent);
		}else{
			return [this._pathModel.toFormName()];
		}
	}

	get formDirective(): any {
		return this._parent ? this._parent.formDirective : null;
	}


	viewToModelUpdate(newValue: any): void {
		this.viewModel = newValue;
		this.arbFormExpressionChange.emit(newValue);
	}

	protected _setUpControl(): void {
		if (!this._pathModel) {
			throw new Error('Attribute crnkFormExpression is required');
		}

		this.name = this._pathModel.toFormName();
		if (this.formDirective) {
			this.formDirective.addControl(this);
			this._registered = true;
		}
	}

	private _checkForErrors(): void {
		if (this._parent) {
			this._checkParentType();
		}
	}

	private _checkParentType(): void {
		if (!(this._parent instanceof NgModelGroup) &&
			this._parent instanceof AbstractFormGroupDirective) {
			TemplateDrivenErrors.formGroupNameException();
		}
		else if (
			!(this._parent instanceof NgModelGroup) && !(this._parent instanceof NgForm)) {
			TemplateDrivenErrors.modelParentException();
		}
	}
}