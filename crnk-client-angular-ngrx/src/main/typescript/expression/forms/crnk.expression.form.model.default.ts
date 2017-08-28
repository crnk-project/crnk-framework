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
	AsyncValidatorFn,
	ControlValueAccessor,
	FormControl,
	NG_ASYNC_VALIDATORS,
	NG_VALIDATORS,
	NG_VALUE_ACCESSOR,
	NgControl,
	Validator,
	ValidatorFn
} from "@angular/forms";
import {
	composeAsyncValidators,
	composeValidators,
	CrnkControl,
	isPropertyUpdated,
	selectValueAccessor,
	setUpControl
} from "./crnk.expression.form.utils";
import {ExpressionAccessor, Path} from "../crnk.expression";


const resolvedPromise = Promise.resolve(null);

const expressionControlBinding: any = {
	provide: NgControl,
	useExisting: forwardRef(() => ExpressionDirective)
};



@Directive({
	selector: '[crnkExpression]',
	providers: [expressionControlBinding],
	exportAs: 'crnkExpression'
})
export class ExpressionDirective extends CrnkControl implements OnChanges, OnDestroy {

	private _pathModel: Path<any>;

	@Input('crnkExpression')
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
	@Output('crnkExpressionChange') crnkExpressionChange = new EventEmitter();

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
		setUpControl(this._control, this);
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
		this.crnkExpressionChange.emit(newValue);
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
