import {Directive, forwardRef, Input} from "@angular/core";
import {
	AbstractControl,
	CheckboxRequiredValidator,
	EmailValidator,
	MaxLengthValidator,
	MinLengthValidator,
	NG_VALIDATORS,
	PatternValidator,
	RequiredValidator,
	Validators
} from "@angular/forms";

export const EXPRESSION_REQUIRED_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionRequiredValidator),
	multi: true
};

@Directive({
	selector:
		':not([type=checkbox])[required][crnkFormExpression],:not([type=checkbox])[required][crnkExpression]',
	providers: [EXPRESSION_REQUIRED_VALIDATOR],
	host: {'[attr.required]': 'required ? "" : null'}
})
export class ExpressionRequiredValidator extends RequiredValidator {

	private _required: boolean;
	private _onChange: () => void;

	@Input()
	get required(): boolean /*| string*/ {
		return this._required;
	}

	set required(value: boolean) {
		this._required = value != null && value !== false && `${value}` !== 'false';
		if (this._onChange) this._onChange();
	}

	validate(c: AbstractControl): { [key: string]: any } {
		console.log("validate!!!", c,  Validators.required(c));
		return this.required ? Validators.required(c) : null;
	}

	registerOnValidatorChange(fn: () => void): void {
		this._onChange = fn;
	}
}


export const EXPRESSION_EMAIL_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionEmailValidator),
	multi: true
};

@Directive({
	selector: '[email][crnkFormExpression],[email][crnkExpression]',
	providers: [EXPRESSION_EMAIL_VALIDATOR]
})
export class ExpressionEmailValidator extends EmailValidator {
}

export const EXPRESSION_CHECKBOX_REQUIRED_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionCheckboxRequiredValidator),
	multi: true
};

@Directive({
	selector:
		'input[type=checkbox][required][crnkFormExpression],input[type=checkbox][required][crnkExpression]',
	providers: [EXPRESSION_CHECKBOX_REQUIRED_VALIDATOR],
	host: {'[attr.required]': 'required ? "" : null'}
})
export class ExpressionCheckboxRequiredValidator extends CheckboxRequiredValidator {
}

export const EXPRESSION_MIN_LENGTH_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionMinLengthValidator),
	multi: true
};


@Directive({
	selector: '[minlength][crnkFormExpression],[minlength][crnkExpression]',
	providers: [EXPRESSION_MIN_LENGTH_VALIDATOR],
	host: {'[attr.minlength]': 'minlength ? minlength : null'}
})
export class ExpressionMinLengthValidator extends MinLengthValidator {
}

export const EXPRESSION_MAX_LENGTH_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionMaxLengthValidator),
	multi: true
};

@Directive({
	selector: '[maxlength][crnkFormExpression],[maxlength][crnkExpression]',
	providers: [EXPRESSION_MAX_LENGTH_VALIDATOR],
	host: {'[attr.maxlength]': 'maxlength ? maxlength : null'}
})
export class ExpressionMaxLengthValidator extends MaxLengthValidator {

}

export const EXPRESSION_PATTERN_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionPatternValidator),
	multi: true
};


@Directive({
	selector: '[pattern][crnkFormExpression],[pattern][crnkExpression]',
	providers: [EXPRESSION_PATTERN_VALIDATOR],
	host: {'[attr.pattern]': 'pattern ? pattern : null'}
})
export class ExpressionPatternValidator extends PatternValidator {

}