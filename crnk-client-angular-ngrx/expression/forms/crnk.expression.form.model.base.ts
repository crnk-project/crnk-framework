import {Input, SimpleChanges} from '@angular/core';
import {
	AsyncValidatorFn,
	ControlContainer,
	ControlValueAccessor,
	FormControl,
	NgControl,
	Validator,
	ValidatorFn
} from '@angular/forms';
import {composeAsyncValidators, composeValidators, isPropertyUpdated} from './crnk.expression.form.utils';
import {Path} from '../crnk.expression';


const resolvedPromise = Promise.resolve(null);


function unimplemented(): any {
	throw new Error('unimplemented');
}

export abstract class CrnkControl extends NgControl {
	/** @internal */
	_parent: ControlContainer = null;
	name: string = null;
	valueAccessor: ControlValueAccessor = null;
	/** @internal */
	_rawValidators: Array<Validator | ValidatorFn> = [];
	/** @internal */
	_rawAsyncValidators: Array<Validator | ValidatorFn> = [];

	protected _pathModel: Path<any>;

	/** @internal */
	protected _control = new FormControl();
	/** @internal */
	protected _registered = false;
	protected viewModel: any;

	@Input('disabled') disabled: false;

	get control(): FormControl {
		return this._control;
	}

	get validator(): ValidatorFn {
		return composeValidators(this._rawValidators);
	}

	get asyncValidator(): AsyncValidatorFn {
		return composeAsyncValidators(this._rawAsyncValidators);
	}

	private _updateValue(value: any): void {
		resolvedPromise.then(
			() => {
				this.control.setValue(value, {emitViewToModelChange: false});
			});
	}

	ngOnChanges(changes: SimpleChanges) {

		if (!this._registered) {
			this._setUpControl();
		}

		if ('disabled' in changes) {
			this._updateDisabled(changes);
		}

		if (isPropertyUpdated(changes, this.viewModel)) {
			const value = this._pathModel.getValue();
			this._updateValue(value);
			this.viewModel = value;
		}
	}

	protected abstract _setUpControl(): void;


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

	abstract viewToModelUpdate(newValue: any): void;
}
