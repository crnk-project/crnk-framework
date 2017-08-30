import {Directive, forwardRef} from "@angular/core";
import {
	CheckboxRequiredValidator,
	EmailValidator,
	MaxLengthValidator,
	MinLengthValidator,
	NG_VALIDATORS,
	PatternValidator,
	RequiredValidator
} from '@angular/forms';

export const EXPRESSION_REQUIRED_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionRequiredValidatorDirective),
	multi: true
};

@Directive({
	selector:
		':not([type=checkbox])[required][crnkFormExpression],:not([type=checkbox])[required][crnkExpression]',
	providers: [EXPRESSION_REQUIRED_VALIDATOR],
	host: {'[attr.required]': 'required ? "" : null'}
})
export class ExpressionRequiredValidatorDirective extends RequiredValidator {
}


export const EXPRESSION_EMAIL_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionEmailValidatorDirective),
	multi: true
};

@Directive({
	selector: '[email][crnkFormExpression],[email][crnkExpression]',
	providers: [EXPRESSION_EMAIL_VALIDATOR]
})
export class ExpressionEmailValidatorDirective extends EmailValidator {
}

export const EXPRESSION_CHECKBOX_REQUIRED_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionCheckboxRequiredValidatorDirective),
	multi: true
};

@Directive({
	selector:
		'input[type=checkbox][required][crnkFormExpression],input[type=checkbox][required][crnkExpression]',
	providers: [EXPRESSION_CHECKBOX_REQUIRED_VALIDATOR],
	host: {'[attr.required]': 'required ? "" : null'}
})
export class ExpressionCheckboxRequiredValidatorDirective extends CheckboxRequiredValidator {
}

export const EXPRESSION_MIN_LENGTH_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionMinLengthValidatorDirective),
	multi: true
};


@Directive({
	selector: '[minlength][crnkFormExpression],[minlength][crnkExpression]',
	providers: [EXPRESSION_MIN_LENGTH_VALIDATOR],
	host: {'[attr.minlength]': 'minlength ? minlength : null'}
})
export class ExpressionMinLengthValidatorDirective extends MinLengthValidator {
}

export const EXPRESSION_MAX_LENGTH_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionMaxLengthValidatorDirective),
	multi: true
};

@Directive({
	selector: '[maxlength][crnkFormExpression],[maxlength][crnkExpression]',
	providers: [EXPRESSION_MAX_LENGTH_VALIDATOR],
	host: {'[attr.maxlength]': 'maxlength ? maxlength : null'}
})
export class ExpressionMaxLengthValidatorDirective extends MaxLengthValidator {

}

export const EXPRESSION_PATTERN_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ExpressionPatternValidatorDirective),
	multi: true
};


@Directive({
	selector: '[pattern][crnkFormExpression],[pattern][crnkExpression]',
	providers: [EXPRESSION_PATTERN_VALIDATOR],
	host: {'[attr.pattern]': 'pattern ? pattern : null'}
})
export class ExpressionPatternValidatorDirective extends PatternValidator {

}