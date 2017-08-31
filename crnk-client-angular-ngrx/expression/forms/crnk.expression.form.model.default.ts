import {Directive, EventEmitter, forwardRef, Inject, Input, OnChanges, OnDestroy, Optional, Output, Self} from '@angular/core';
import {
	AsyncValidatorFn,
	ControlValueAccessor,
	NG_ASYNC_VALIDATORS,
	NG_VALIDATORS,
	NG_VALUE_ACCESSOR,
	NgControl,
	Validator,
	ValidatorFn
} from '@angular/forms';
import {selectValueAccessor, setUpControl} from './crnk.expression.form.utils';
import {ExpressionAccessor, Path} from '../crnk.expression';
import {CrnkControl} from './crnk.expression.form.model.base';


const expressionControlBinding: any = {
	provide: NgControl,
	useExisting: forwardRef(() => ExpressionDirective)
};


@Directive({
	selector: '[crnkExpression]',
	providers: [expressionControlBinding],
	exportAs: 'crnkExpression'
})
export class ExpressionDirective extends CrnkControl implements OnChanges, OnDestroy {

	public get pathModel() {
		return this._pathModel;
	}


	@Input('crnkExpression')
	public set pathModel(pathModel: Path<any>) {
		this._pathModel = pathModel;

		const expressionAccessor = this.valueAccessor as any as ExpressionAccessor;
		if (expressionAccessor.setExpression) {
			expressionAccessor.setExpression(pathModel);
		}
	}

	@Output('crnkExpressionChange') crnkExpressionChange = new EventEmitter();

	/**
	 * NOTE that in contract to NgModel we do not use @Host for parent. This would enforce that the
	 * components are direct children of the forms component. TODO investigate whether there is
	 * any better solutions (maybe something similar to FormGroup). It does not seem like it is possible
	 * to expose an existing ControlContainer again with @Component.providers as it would lead to a circular
	 * dependency (there is no provider for children only, but itself as well).
	 */
	constructor(
		@Optional() @Self() @Inject(NG_VALIDATORS) validators: Array<Validator | ValidatorFn>,
		@Optional() @Self() @Inject(NG_ASYNC_VALIDATORS) asyncValidators: Array<Validator | AsyncValidatorFn>,
		@Optional() @Self() @Inject(NG_VALUE_ACCESSOR) valueAccessors: ControlValueAccessor[]
	) {
		super();
		this._rawValidators = validators || [];
		this._rawAsyncValidators = asyncValidators || [];
		this.valueAccessor = selectValueAccessor(this, valueAccessors);
	}


	ngOnDestroy(): void {
	}

	protected _setUpControl(): void {
		this._setUpStandalone();

		this._registered = true;
	}


	private _setUpStandalone(): void {
		setUpControl(this._control, this);
		this._control.updateValueAndValidity({emitEvent: false});
	}

	viewToModelUpdate(newValue: any): void {
		this.viewModel = newValue;
		this.crnkExpressionChange.emit(newValue);
	}


}
