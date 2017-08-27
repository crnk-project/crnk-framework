import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ExpressionDirective} from './crnk.expression.form.model.default';
import {
	ExpressionCheckboxRequiredValidator,
	ExpressionMaxLengthValidator,
	ExpressionMinLengthValidator,
	ExpressionPatternValidator,
	ExpressionRequiredValidator
} from "./crnk.expression.form.validators";
import {FormExpressionDirective} from "./crnk.expression.form.model.form";
import {ExpressionDefaultValueAccessorDirective} from "./crnk.expression.form.model.accessor";

/**
 * Adds support for the {@link FormExpressionDirective} to bind expressions to expectForm controls.
 */
@NgModule({
	imports: [FormsModule],
	exports: [
		FormsModule, ExpressionDirective, FormExpressionDirective, ExpressionDefaultValueAccessorDirective,

		ExpressionPatternValidator, ExpressionMaxLengthValidator, ExpressionMinLengthValidator, ExpressionCheckboxRequiredValidator,
		ExpressionRequiredValidator
	],
	declarations: [
		ExpressionDirective, FormExpressionDirective, ExpressionDefaultValueAccessorDirective,

		ExpressionPatternValidator, ExpressionMaxLengthValidator, ExpressionMinLengthValidator, ExpressionCheckboxRequiredValidator,
		ExpressionRequiredValidator
	]
})
export class CrnkExpressionModule {
}
