import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ExpressionDirective} from './crnk.expression.form.model.default';
import {
	ExpressionCheckboxRequiredValidatorDirective, ExpressionEmailValidatorDirective,
	ExpressionMaxLengthValidatorDirective,
	ExpressionMinLengthValidatorDirective,
	ExpressionPatternValidatorDirective,
	ExpressionRequiredValidatorDirective
} from "./crnk.expression.form.validators.base";
import {FormExpressionDirective} from "./crnk.expression.form.model.form";
import {ExpressionDefaultValueAccessorDirective} from "./crnk.expression.form.model.accessor";
import {ResourceValidatorDirective} from './crnk.expression.form.validators.jsonapi';

/**
 * Adds support for the {@link FormExpressionDirective} to bind expressions to expectForm controls.
 */
@NgModule({
	imports: [FormsModule],
	exports: [
		FormsModule, ExpressionDirective, FormExpressionDirective, ExpressionDefaultValueAccessorDirective,

		ExpressionPatternValidatorDirective, ExpressionMaxLengthValidatorDirective, ExpressionMinLengthValidatorDirective, ExpressionCheckboxRequiredValidatorDirective,
		ExpressionRequiredValidatorDirective, ExpressionEmailValidatorDirective, ResourceValidatorDirective
	],
	declarations: [
		ExpressionDirective, FormExpressionDirective, ExpressionDefaultValueAccessorDirective,

		ExpressionPatternValidatorDirective, ExpressionMaxLengthValidatorDirective, ExpressionMinLengthValidatorDirective, ExpressionCheckboxRequiredValidatorDirective,
		ExpressionRequiredValidatorDirective, ExpressionEmailValidatorDirective, ResourceValidatorDirective
	]
})
export class CrnkExpressionFormModule {
}
