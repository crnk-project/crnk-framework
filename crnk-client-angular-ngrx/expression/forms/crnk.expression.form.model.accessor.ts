import {Directive, ElementRef, forwardRef, Renderer2} from "@angular/core";
import {DefaultValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";


export const DEFAULT_VALUE_ACCESSOR: any = {
	provide: NG_VALUE_ACCESSOR,
	useExisting: forwardRef(() => ExpressionDefaultValueAccessorDirective),
	multi: true
};

@Directive({
	selector: '[crnkFormExpression],[crnkExpression]',
	host: {'(input)': 'onChange($event.target.value)', '(blur)': 'onTouched()'},
	providers: [DEFAULT_VALUE_ACCESSOR]
})
export class ExpressionDefaultValueAccessorDirective extends DefaultValueAccessor {

	constructor(_renderer: Renderer2, _elementRef: ElementRef) {
		super(_renderer, _elementRef, false);
	}
}

