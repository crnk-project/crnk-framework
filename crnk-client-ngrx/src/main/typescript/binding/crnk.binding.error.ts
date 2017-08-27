import {
	AfterViewChecked, AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ContentChild, Input, OnInit,
	TemplateRef
} from '@angular/core';
import {AbstractControl, NgForm} from '@angular/forms';
import {ResourceError} from 'ngrx-json-api';
import {Path} from "../expression/crnk.expression";

import * as _ from "lodash";


/**
 * Displays the errors of a control. The control instance is selected by passing its expression.
 */
@Component({
	selector: 'crnk-control-errors',
	template: `
		<div *ngFor="let error of controlErrors">
			<ng-container [ngTemplateOutlet]="template" [ngOutletContext]="{error: error}"></ng-container>
		</div>
	`,
})
export class ControlErrorsComponent {

	private _expression: Path<any>;

	private control: AbstractControl;

	private _controlErrors: Array<any> = [];

	@ContentChild(TemplateRef) template;

	constructor(private form: NgForm) {
	}

	@Input()
	public set expression(expression: Path<any>) {
		this._expression = expression;
		this.collectErrors();
	}

	public get controlErrors(){
		if(this.control == null){
			this.collectErrors();
		}
		return this._controlErrors;
	}

	private collectErrors(){
		const formName = this._expression.toFormName();
		this.control = this.form.controls[formName];
		if (this.control && this.control.errors) {
			this._controlErrors = _.values(this.control.errors);
		}
		else {
			this._controlErrors = undefined;
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
			<ng-container [ngTemplateOutlet]="template" [ngOutletContext]="{error: error}"></ng-container>
		</div>
	`,
})
export class ResourceErrorsComponent {

	public pathErrors: Array<any> = [];

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
		const pathErrors = [];
		if (resource && resource.errors) {
			const errors = resource.errors as Array<ResourceError>;
			for (const error of errors) {
				if (error.source && error.source.pointer) {
					const errorPath = this.toPath(error.source.pointer);
					if (path === errorPath) {
						pathErrors.push(error);
					}
				}
			}
		}
		this.pathErrors = pathErrors

		this.cd.markForCheck();
	}
}

