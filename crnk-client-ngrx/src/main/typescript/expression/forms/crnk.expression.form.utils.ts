////////////////////////////////////////////////////////////////////////////////////////////////////////////
// copy/pasted from @angular/forms to due the lack of customizability of their implementation
////////////////////////////////////////////////////////////////////////////////////////////////////////////
import {
	ControlContainer,
	Validator,
	ControlValueAccessor,
	AsyncValidatorFn,
	ValidatorFn,
	FormControl,
	NgControl,
	AbstractControlDirective,
	Validators,
	CheckboxControlValueAccessor,
	AbstractControl,
	SelectControlValueAccessor,
	SelectMultipleControlValueAccessor,
	DefaultValueAccessor,
	RadioControlValueAccessor
} from "@angular/forms";


export function normalizeValidator(validator: ValidatorFn | Validator): ValidatorFn {
	if ((<Validator>validator).validate) {
		return (c: AbstractControl) => (<Validator>validator).validate(c);
	}
	else {
		return <ValidatorFn>validator;
	}
}

export function normalizeAsyncValidator(validator: AsyncValidatorFn): AsyncValidatorFn {
	// TODO experimental functionality disabled as not exported by Angular
	/*if ((<AsyncValidator>validator).validate) {
	 return (c: AbstractControl) => (<AsyncValidator>validator).validate(c);
	 } else {
	 return <AsyncValidatorFn>validator;
	 }*/
	return <AsyncValidatorFn>validator;
}

export function looseIdentical(a: any, b: any): boolean {
	return a === b || typeof a === 'number' && typeof b === 'number' && isNaN(a) && isNaN(b);
}

export function isPresent(obj: any): boolean {
	return obj !== null;
}


export function controlPath(name: string, parent: ControlContainer): string[] {
	return [...parent.path, name];
}

export function setUpControl(control: FormControl, dir: CrnkControl): void {
	if (!control) {
		_throwError(dir, 'Cannot find control with');
	}
	if (!dir.valueAccessor) {
		_throwError(dir, 'No value accessor for form control with');
	}

	control.validator = Validators.compose([control.validator, dir.validator]);
	control.asyncValidator = Validators.composeAsync([control.asyncValidator, dir.asyncValidator]);
	dir.valueAccessor.writeValue(control.value);

	// view -> model
	dir.valueAccessor.registerOnChange((newValue: any) => {
		dir.viewToModelUpdate(newValue);
		control.markAsDirty();
		control.setValue(newValue, {emitModelToViewChange: false});
	});

	// touched
	dir.valueAccessor.registerOnTouched(() => control.markAsTouched());

	control.registerOnChange((newValue: any, emitModelEvent: boolean) => {
		// control -> view
		dir.valueAccessor.writeValue(newValue);

		// control -> ngModel
		if (emitModelEvent) {
			dir.viewToModelUpdate(newValue);
		}
	});

	if (dir.valueAccessor.setDisabledState) {
		control.registerOnDisabledChange(
			(isDisabled: boolean) => {
				dir.valueAccessor.setDisabledState(isDisabled);
			});
	}

	// re-run validation when validator binding changes, e.g. minlength=3 -> minlength=4
	dir._rawValidators.forEach((validator: Validator | ValidatorFn) => {
		if ((<Validator>validator).registerOnValidatorChange) {
			(<Validator>validator).registerOnValidatorChange(() => control.updateValueAndValidity());
		}
	});

	dir._rawAsyncValidators.forEach((validator: Validator | ValidatorFn) => {
		if ((<Validator>validator).registerOnValidatorChange) {
			(<Validator>validator).registerOnValidatorChange(() => control.updateValueAndValidity());
		}
	});
}


function _noControlError(dir: NgControl) {
	return _throwError(dir, 'There is no FormControl instance attached to form control element with');
}

function _throwError(dir: AbstractControlDirective, message: string): void {
	let messageEnd: string;
	if (dir.path.length > 1) {
		messageEnd = `path: '${dir.path.join(' -> ')}'`;
	}
	else if (dir.path[0]) {
		messageEnd = `name: '${dir.path}'`;
	}
	else {
		messageEnd = 'unspecified name attribute';
	}
	throw new Error(`${message} ${messageEnd}`);
}

export function composeValidators(validators: Array<Validator|Function>): ValidatorFn {
	return isPresent(validators) ? Validators.compose(validators.map(normalizeValidator)) : null;
}

export function composeAsyncValidators(validators: Array<Validator|Function>): AsyncValidatorFn {
	return isPresent(validators) ? Validators.composeAsync(validators.map(normalizeAsyncValidator)) :
		null;
}

export function isPropertyUpdated(changes: {[key: string]: any}, viewModel: any): boolean {
	if (!changes.hasOwnProperty('pathModel')) {
		return false;
	}
	const change = changes['pathModel'];

	if (change.isFirstChange()) {
		return true;
	}
	return !looseIdentical(viewModel, change.currentValue);
}

const BUILTIN_ACCESSORS = [
	CheckboxControlValueAccessor,
	// TODO RangeValueAccessor,
	// TODO NumberValueAccessor,
	SelectControlValueAccessor,
	SelectMultipleControlValueAccessor,
	RadioControlValueAccessor,
];

export function isBuiltInAccessor(valueAccessor: ControlValueAccessor): boolean {
	return BUILTIN_ACCESSORS.some(a => valueAccessor.constructor === a);
}

// TODO: vsavkin remove it once https://github.com/angular/angular/issues/3011 is implemented
export function selectValueAccessor(dir: NgControl, valueAccessors: ControlValueAccessor[]): ControlValueAccessor {
	if (!valueAccessors) {
		return null;
	}

	let defaultAccessor: ControlValueAccessor;
	let builtinAccessor: ControlValueAccessor;
	let customAccessor: ControlValueAccessor;
	valueAccessors.forEach((v: ControlValueAccessor) => {
		if (v.constructor === DefaultValueAccessor) {
			defaultAccessor = v;

		}
		else if (isBuiltInAccessor(v)) {
			if (builtinAccessor) {
				_throwError(dir, 'More than one built-in value accessor matches form control with');
			}
			builtinAccessor = v;

		}
		else {
			if (customAccessor) {
				_throwError(dir, 'More than one custom value accessor matches form control with');
			}
			customAccessor = v;
		}
	});

	if (customAccessor) {
		return customAccessor;
	}
	if (builtinAccessor) {
		return builtinAccessor;
	}
	if (defaultAccessor) {
		return defaultAccessor;
	}

	_throwError(dir, 'No valid value accessor for form control with');
	return null;
}


export class TemplateDrivenErrors {
	static modelParentException(): void {
		throw new Error(`
      ngModel cannot be used to register form controls with a parent formGroup directive.`);
	}

	static formGroupNameException(): void {
		throw new Error(`
      ngModel cannot be used to register form controls with a parent formGroupName or formArrayName directive.`);
	}

	static missingNameException() {
		throw new Error(
			`If ngModel is used within a form tag, either the name attribute must be set or the form
      control must be defined as 'standalone' in ngModelOptions.
      Example 1: <input [(ngModel)]="person.firstName" name="first">
      Example 2: <input [(ngModel)]="person.firstName" [ngModelOptions]="{standalone: true}">`);
	}

	static modelGroupParentException() {
		throw new Error(`
      ngModelGroup cannot be used with a parent formGroup directive.`);
	}
}

function unimplemented(): any {
	throw new Error('unimplemented');
}

export abstract class CrnkControl extends NgControl {
	/** @internal */
	_parent: ControlContainer = null;
	name: string = null;
	valueAccessor: ControlValueAccessor = null;
	/** @internal */
	_rawValidators: Array<Validator|ValidatorFn> = [];
	/** @internal */
	_rawAsyncValidators: Array<Validator|ValidatorFn> = [];

	get validator(): ValidatorFn {
		return <ValidatorFn>unimplemented();
	}

	get asyncValidator(): AsyncValidatorFn {
		return <AsyncValidatorFn>unimplemented();
	}

	abstract viewToModelUpdate(newValue: any): void;
}
