import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessor} from './crnk.expression.form.model';

/**
 * Adds support for the {@link FormExpressionDirective} to bind expressions to expectForm controls.
 */
@NgModule({
	imports: [FormsModule],
	exports: [FormsModule, ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessor],
	declarations: [ExpressionDirective, FormExpressionDirective, PathDefaultValueAccessor]
})
export class ArbExpressionModule {
}
