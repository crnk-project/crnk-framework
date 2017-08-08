import {
	Directive,
	ElementRef,
	EventEmitter,
	forwardRef,
	Inject,
	Input,
	OnChanges,
	OnDestroy,
	Optional,
	Output,
	Renderer,
	Self,
	SimpleChanges
} from "@angular/core";
import {
	AbstractFormGroupDirective,
	AsyncValidatorFn,
	ControlContainer,
	ControlValueAccessor,
	DefaultValueAccessor,
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
	ArbControl,
	composeAsyncValidators,
	composeValidators,
	controlPath,
	isPropertyUpdated,
	selectValueAccessor,
	TemplateDrivenErrors
} from "./crnk.expression.form.utils";
import {ExpressionAccessor, Path} from "../crnk.expression";


const resolvedPromise = Promise.resolve(null);

const formControlBinding: any = {
	provide: NgControl,
	useExisting: forwardRef(() => FormExpressionDirective)
};

/**
 * Allows to bind expressions of type {link Expression} to expectForm controls. The directive is very similar to
 * NgModel, but allows to bind an expression instead of a generic object. The advantage is that an expression
 * can hold both
 */
@Directive({
	selector: '[arbFormExpression]:not([formControlName]):not([formControl])',
	providers: [formControlBinding],
	exportAs: 'arbFormExpression'
})
export class FormExpressionDirective extends ArbControl implements OnChanges, OnDestroy {

	private _pathModel: Path<any>;

	@Input('arbFormExpression')
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
			throw new Error('Attribute arbFormExpression is required');
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

export const DEFAULT_VALUE_ACCESSOR: any = {
	provide: NG_VALUE_ACCESSOR,
	useExisting: forwardRef(() => PathDefaultValueAccessorDirective),
	multi: true
};

@Directive({
	selector: 'input:not([type=checkbox])[arbExpression],textarea[arbExpression],input:not([type=checkbox])[arbFormExpression],textarea[arbFormExpression]',
	host: {'(input)': 'onChange($event.target.value)', '(blur)': 'onTouched()'},
	providers: [DEFAULT_VALUE_ACCESSOR]
})
export class PathDefaultValueAccessorDirective extends DefaultValueAccessor {

	constructor(_renderer: Renderer, _elementRef: ElementRef) {
		super(_renderer, _elementRef, false);
	}
}


function setUpFormControl(control: FormControl, dir: NgControl): void {
	if (!control) {
		throw new Error('Cannot find control with');
	}
	if (!dir.valueAccessor) {
		throw new Error('No value accessor for form control with');
	}

	//control.validator = Validators.compose([control.validator !, dir.validator]);
	//control.asyncValidator = Validators.composeAsync([control.asyncValidator !, dir.asyncValidator]);

	dir.valueAccessor !.writeValue(control.value);

	// view -> model
	dir.valueAccessor !.registerOnChange((newValue: any) => {
		dir.viewToModelUpdate(newValue);
		control.markAsDirty();
		control.setValue(newValue, {emitModelToViewChange: false});
	});

	// touched
	dir.valueAccessor !.registerOnTouched(() => control.markAsTouched());

	control.registerOnChange((newValue: any, emitModelEvent: boolean) => {
		// control -> view
		dir.valueAccessor !.writeValue(newValue);

		// control -> ngModel
		if (emitModelEvent) {
			dir.viewToModelUpdate(newValue);
		}
	});

	if (dir.valueAccessor !.setDisabledState) {
		control.registerOnDisabledChange(
			(disabled: boolean) => {
				dir.valueAccessor !.setDisabledState !(disabled);
			});
	}

	// re-run validation when validator binding changes, e.g. minlength=3 -> minlength=4
	/*dir._rawValidators.forEach((validator: Validator | ValidatorFn) => {
	 if ((<Validator>validator).registerOnValidatorChange)
	 (<Validator>validator).registerOnValidatorChange !(() => control.updateValueAndValidity());
	 });

	 dir._rawAsyncValidators.forEach((validator: AsyncValidator | AsyncValidatorFn) => {
	 if ((<Validator>validator).registerOnValidatorChange)
	 (<Validator>validator).registerOnValidatorChange !(() => control.updateValueAndValidity());
	 });*/
}


@Directive({
	selector: '[arbExpression]:not([formControlName]):not([formControl])',
	providers: [formControlBinding],
	exportAs: 'arbExpression'
})
export class ExpressionDirective extends ArbControl implements OnChanges, OnDestroy {

	private _pathModel: Path<any>;

	@Input('arbExpression')
	public set pathModel(pathModel: Path<any>) {
		this._pathModel = pathModel;

		const expressionAccessor = this.valueAccessor as any as ExpressionAccessor;
		if (expressionAccessor.setExpression) {
			expressionAccessor.setExpression(pathModel);
		}
	}

	public get pathModel() {
		return this._pathModel;
	}


	/** @internal */
	_control = new FormControl();

	viewModel: any;

	_registered = false;

	@Input('disabled') disabled: boolean;
	@Output('arbExpressionChange') arbExpressionChange = new EventEmitter();

	/**
	 * NOTE that in contract to NgModel we do not use @Host for parent. This would enforce that the
	 * components are direct children of the forms component. TODO investigate whether there is
	 * any better solutions (maybe something similar to FormGroup). It does not seem like it is possible
	 * to expose an existing ControlContainer again with @Component.providers as it would lead to a circular
	 * dependency (there is no provider for children only, but itself as well).
	 */
	constructor(@Optional() @Self() @Inject(NG_VALIDATORS) validators: Array<Validator | ValidatorFn>,
				@Optional() @Self() @Inject(NG_ASYNC_VALIDATORS) asyncValidators: Array<Validator | AsyncValidatorFn>,
				@Optional() @Self() @Inject(NG_VALUE_ACCESSOR) valueAccessors: ControlValueAccessor[]) {
		super();
		this._rawValidators = validators || [];
		this._rawAsyncValidators = asyncValidators || [];
		this.valueAccessor = selectValueAccessor(this, valueAccessors);
	}

	ngOnChanges(changes: SimpleChanges) {

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
	}

	private _setUpControl(): void {
		this._setUpStandalone();
		this._registered = true;
	}


	private _setUpStandalone(): void {
		setUpFormControl(this._control, this);
		this._control.updateValueAndValidity({emitEvent: false});
	}

	get control(): FormControl {
		return this._control;
	}

	get validator(): ValidatorFn {
		return composeValidators(this._rawValidators);
	}

	get asyncValidator(): AsyncValidatorFn {
		return composeAsyncValidators(this._rawAsyncValidators);
	}

	viewToModelUpdate(newValue: any): void {
		this.viewModel = newValue;
		this.arbExpressionChange.emit(newValue);
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
