import {
	Directive,
	EventEmitter,
	forwardRef,
	Inject,
	Input,
	OnChanges,
	OnDestroy,
	Optional,
	Output,
	Self,
	SimpleChanges
} from "@angular/core";
import {
	AbstractFormGroupDirective,
	AsyncValidatorFn,
	ControlContainer,
	ControlValueAccessor,
	FormControl,
	NG_ASYNC_VALIDATORS,
	NG_VALIDATORS,
	NG_VALUE_ACCESSOR,
	NgControl,
	NgForm,
	NgModelGroup,
	Validator,
	ValidatorFn
} from "@angular/forms";
import {
	composeAsyncValidators,
	composeValidators,
	controlPath,
	CrnkControl,
	isPropertyUpdated,
	selectValueAccessor,
	TemplateDrivenErrors
} from "./crnk.expression.form.utils";
import {ExpressionAccessor, Path} from "../crnk.expression";

const formExpressionBinding: any = {
	provide: NgControl,
	useExisting: forwardRef(() => FormExpressionDirective)
};


const resolvedPromise = Promise.resolve(null);

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

	private _pathModel: Path<any>;

	@Input('crnkFormExpression')
	public set pathModel(pathModel: Path<any>) {
		this._pathModel = pathModel;

		const expressionAccessor = this.valueAccessor as any as ExpressionAccessor;
		if (!expressionAccessor) {
			throw new Error("no value accessor found");
		}
		if (expressionAccessor.setExpression) {
			expressionAccessor.setExpression(pathModel);
		}
	}

	public get pathModel() {
		return this._pathModel;
	}


	/** @internal */
	_control = new FormControl();
	/** @internal */
	_registered = false;
	viewModel: any;

	name: string;

	@Input('disabled') disabled: false;
	@Output('arbFormExpressionChange') arbFormExpressionChange = new EventEmitter();

	/**
	 * NOTE that in contract to NgModel we do not use @Host for parent. This would enforce that the
	 * components are direct children of the forms component. TODO investigate whether there is
	 * any better solutions (maybe something similar to FormGroup). It does not seem like it is possible
	 * to expose an existing ControlContainer again with @Component.providers as it would lead to a circular
	 * dependency (there is no provider for children only, but itself as well).
	 */
	constructor(@Optional() parent: ControlContainer,
				@Optional() @Self() @Inject(NG_VALIDATORS) validators: Array<Validator | ValidatorFn>,
				@Optional() @Self() @Inject(NG_ASYNC_VALIDATORS) asyncValidators: Array<Validator | AsyncValidatorFn>,
				@Optional() @Self() @Inject(NG_VALUE_ACCESSOR) valueAccessors: ControlValueAccessor[]) {
		super();
		this._parent = parent;
		this._rawValidators = validators || [];
		this._rawAsyncValidators = asyncValidators || [];
		this.valueAccessor = selectValueAccessor(this, valueAccessors);
	}

	ngOnChanges(changes: SimpleChanges) {
		this._checkForErrors();
		if (!this._registered) {
			this._setUpControl();
		}
		if ('disabled' in changes) {
			this._updateDisabled(changes);
		}

		if (isPropertyUpdated(changes, this.viewModel)) {
			const value = this.pathModel.getValue();
			this._updateValue(value);
			this.viewModel = value;
		}
	}

	ngOnDestroy(): void {
		if (this.formDirective) {
			this.formDirective.removeControl(this);
		}
	}

	get control(): FormControl {
		return this._control;
	}

	get path(): string[] {
		return this._parent ? controlPath(this.pathModel.toFormName(), this._parent) : [this.pathModel.toFormName()];
	}

	get formDirective(): any {
		return this._parent ? this._parent.formDirective : null;
	}

	get validator(): ValidatorFn {
		return composeValidators(this._rawValidators);
	}

	get asyncValidator(): AsyncValidatorFn {
		return composeAsyncValidators(this._rawAsyncValidators);
	}

	viewToModelUpdate(newValue: any): void {
		this.viewModel = newValue;
		this.arbFormExpressionChange.emit(newValue);
	}

	private _setUpControl(): void {
		if (!this.pathModel) {
			throw new Error('Attribute crnkFormExpression is required');
		}

		this.name = this.pathModel.toFormName();
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

	private _updateValue(value: any): void {
		resolvedPromise.then(
			() => {
				this.control.setValue(value, {emitViewToModelChange: false});
			});
	}

	private _updateDisabled(changes: SimpleChanges) {
		const disabledValue = changes['disabled'].currentValue;

		const disabled =
			disabledValue === '' || (disabledValue && disabledValue !== 'false');

		resolvedPromise.then(() => {
			if (disabled && !this.control.disabled) {
				this.control.disable();
			}
			else if (!disabled && this.control.disabled) {
				this.control.enable();
			}
		});
	}
}