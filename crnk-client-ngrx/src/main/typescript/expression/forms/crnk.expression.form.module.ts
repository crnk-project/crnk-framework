import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessorDirective} from './crnk.expression.form.model';

/**
 * Adds support for the {@link FormExpressionDirective} to bind expressions to expectForm controls.
 */
@NgModule({
	imports: [FormsModule],
	exports: [FormsModule, ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessorDirective],
	declarations: [ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessorDirective]
})
export class ArbExpressionModule {
}
