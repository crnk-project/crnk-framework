import {Directive, forwardRef} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Path} from '../crnk.expression';
import {ResourceError, StoreResource} from 'ngrx-json-api';
import * as _ from 'lodash';

export const RESOURCE_VALIDATOR: any = {
	provide: NG_VALIDATORS,
	useExisting: forwardRef(() => ResourceValidatorDirective),
	multi: true
};

/**
 * Gets validation messages from the underlying resource.
 */
@Directive({
	selector:
		'[crnkFormExpression],[crnkExpression]',
	providers: [RESOURCE_VALIDATOR]
})
export class ResourceValidatorDirective implements Validator {

	validate(c: AbstractControl): ValidationErrors | null {

		const path = c['_pathModel'] as Path<any>;

		if (!path) {
			throw new Error('ResourceValidatorDirective can only be used with crnkFormExpression and crnkExpression');
		}

		// note that invalid changes to not get pushed to the store. In this case the controlValue
		// and the resourceValue will differ and resource errors are no longer deemed useful.
		// Otherwise either errorous values would have to be pushed as well or the component could never
		// recover from an invalid state;
		let controlValue = c.value;
		let resourceValue = path.getValue();
		let dirty = !_.isEqual(controlValue, resourceValue);

		const resource = path.getResource() as StoreResource;
		if (resource && resource.errors && !dirty) {
			const sourcePointer = path.getSourcePointer();

			let validationErrors: ValidationErrors = {};
			for (const resourceError of resource.errors) {
				const errorKey = this.computeControlErrorKey(resourceError);
				if (resourceError.source && sourcePointer === resourceError.source.pointer && errorKey) {
					validationErrors[errorKey] = resourceError;
				}
			}
			return validationErrors;
		}

		return null;
	}

	private computeControlErrorKey(error: ResourceError) {
		return error.code ? error.code : error.id;
	}
}
