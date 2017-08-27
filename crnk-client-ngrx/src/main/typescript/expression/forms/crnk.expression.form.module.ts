import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
	ExpressionDirective,
	FormExpressionDirective,
	PathDefaultValueAccessorDirective
} from './crnk.expression.form.model';
import {
	ExpressionCheckboxRequiredValidator,
	ExpressionMaxLengthValidator,
	ExpressionMinLengthValidator,
	ExpressionPatternValidator,
	ExpressionRequiredValidator
} from "./crnk.expression.form.validators";

/**
 * Adds support for the {@link FormExpressionDirective} to bind expressions to expectForm controls.
 */
@NgModule({
	imports: [FormsModule],
	exports: [
		FormsModule, ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessorDirective,

		ExpressionPatternValidator, ExpressionMaxLengthValidator, ExpressionMinLengthValidator, ExpressionCheckboxRequiredValidator,
		ExpressionRequiredValidator
	],
	declarations: [
		ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessorDirective,

		ExpressionPatternValidator, ExpressionMaxLengthValidator, ExpressionMinLengthValidator, ExpressionCheckboxRequiredValidator,
		ExpressionRequiredValidator
	]
})
export class CrnkExpressionModule {
}
