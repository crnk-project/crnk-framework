import {
	ChangeDetectionStrategy,
	ChangeDetectorRef,
	Component,
	ContentChild,
	Input,
	OnDestroy,
	TemplateRef
} from '@angular/core';
import {AbstractControl, NgForm} from '@angular/forms';
import {ResourceError} from 'ngrx-json-api';
import {Path} from '../expression/crnk.expression';
import {Subscription} from 'rxjs/Subscription';


export interface ErrorEntry {
	code: string;
	data: any;
}

/**
 * Displays the errors of a control. The control instance is selected by passing its expression.
 */
@Component({
	selector: 'crnk-control-errors',
	changeDetection: ChangeDetectionStrategy.OnPush,
	template: `
		<div *ngFor="let error of controlErrors">
			<ng-container [ngTemplateOutlet]="template"
						  [ngOutletContext]="{errorCode: error.code, errorData: error.data}"></ng-container>
		</div>
	`,
})
export class ControlErrorsComponent implements OnDestroy {

	private _expression: Path<any>;

	private control: AbstractControl;

	private controlValueSubscription: Subscription;

	private controlStatusSubscription: Subscription;

	private formSubscription: Subscription;

	public controlErrors: Array<ErrorEntry> = [];

	@ContentChild(TemplateRef) template;

	constructor(private form: NgForm, private cd: ChangeDetectorRef) {
	}

	@Input()
	public set expression(expression: Path<any>) {
		this._expression = expression;
		this.collectErrors();
	}

	ngOnDestroy(): void {
		if (this.controlValueSubscription) {
			this.controlValueSubscription.unsubscribe();
			this.controlValueSubscription = null;
		}
		if (this.controlStatusSubscription) {
			this.controlStatusSubscription.unsubscribe();
			this.controlStatusSubscription = null;
		}
		if (this.formSubscription) {
			this.formSubscription.unsubscribe();
			this.formSubscription = null;
		}
		this.control = null;
	}

	private initControl() {
		if (!this.control) {
			const formName = this._expression.toFormName();
			this.control = this.form.controls[formName];
			if (this.control) {
				this.controlValueSubscription = this.control.valueChanges.subscribe(() => this.collectErrors());
				this.controlStatusSubscription = this.control.statusChanges.subscribe(() => this.collectErrors());

				// no longer needed once we have a reference to the control
				if (this.formSubscription != null) {
					this.formSubscription.unsubscribe();
					this.formSubscription = null;
				}
			}
			else if (!this.formSubscription) {
				// listen to form till we have a reference to the control
				this.formSubscription = this.form.valueChanges.subscribe(() => this.collectErrors());
			}
		}
	}


	private collectErrors() {
		this.initControl();
		if (this.control) {
			const newControlErrors = [];

			const errors = this.control.errors;
			for (const errorCode in errors) {
				if (errors.hasOwnProperty(errorCode)) {
					newControlErrors.push({
						code: errorCode,
						data: errors[errorCode]
					});
				}
			}

			this.controlErrors = newControlErrors;

			this.cd.markForCheck();
		}
		else {
			this.controlErrors = undefined;
		}
	}
}


/**
 * Displays the errors for the given field for a resource. The field and resource is passed to this component
 * as expression.
 */
@Component({
	selector: 'crnk-resource-errors',
	changeDetection: ChangeDetectionStrategy.OnPush,
	template: `
		<div *ngFor="let error of pathErrors">
			<ng-container [ngTemplateOutlet]="template"
						  [ngOutletContext]="{errorCode: error.code, errorData: error.data}"></ng-container>
		</div>
	`,
})
export class ResourceErrorsComponent {

	public pathErrors: Array<ErrorEntry> = [];

	@ContentChild(TemplateRef) template;

	constructor(private cd: ChangeDetectorRef) {
	}

	protected toPath(sourcePointer: string) {
		let formName = sourcePointer.replace(new RegExp('/', 'g'), '.');
		if (formName.startsWith('.')) {
			formName = formName.substring(1);
		}
		if (formName.endsWith('.')) {
			formName = formName.substring(0, formName.length - 1);
		}
		if (formName.startsWith('data.')) {
			formName = formName.substring(5);
		}
		return formName;
	}


	@Input()
	public set expression(expression: Path<any>) {
		const path = expression.toString();
		const resource = expression.getResource();
		const pathErrors: Array<ErrorEntry> = [];
		if (resource && resource.errors) {
			const errors = resource.errors as Array<ResourceError>;
			for (const error of errors) {
				if (error.source && error.source.pointer && error.code) {
					const errorPath = this.toPath(error.source.pointer);
					if (path === errorPath) {
						pathErrors.push({
							code: error.code,
							data: error
						});
					}
				}
			}
		}
		this.pathErrors = pathErrors;

		this.cd.markForCheck();
	}
}

