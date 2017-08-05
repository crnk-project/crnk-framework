import {Observable} from "rxjs/Observable";
import {Subscription} from "rxjs/Subscription";
import * as _ from "lodash";
import "rxjs/add/operator/zip";
import "rxjs/add/operator/do";
import "rxjs/add/operator/debounceTime";
import "rxjs/add/operator/distinct";
import "rxjs/add/operator/switch";
import "rxjs/add/operator/finally";
import "rxjs/add/operator/share";
import {NgForm} from "@angular/forms";
import {OperationsService} from "../operations";
import {NgrxJsonApiService, ResourceError, Resource, StoreResource, ResourceIdentifier} from "ngrx-json-api";

export interface FormBindingConfig {
	/**
	 * Reference to the forFormElement instance to hook into.
	 */
	form: NgForm;

	/**
	 * Reference to a query from the store to get notified about validation errors.
	 * FormBinding implementation assumes that the query has already been executed
	 * (typically when performing the route to a new page).
	 */
	queryId: string;
}

/**
 * Binding between ngrx-jsonapi and angular forms. It serves two purposes:
 *
 * <ul>
 *     <li>Updates the JSON API store when forFormElement controls changes their values.</li>
 *     <li>Updates the validation state of forFormElement controls in case of JSON API errors. JSON API errors that cannot be
 *         mapped to a forFormElement control are hold in the errors property
 *     </li>
 * <ul>
 *
 * The binding between resources in the store and forFormElement controls happens trough the naming of the forFormElement
 * controls. Two naming patterns are supported:
 *
 * <ul>
 *     <li>basic binding for all forFormElement controls that start with "attributes." or "relationships.". A forFormElement
 * control with label "attributes.title" is mapped to the "title" attribute of the JSON API resource in the store. The id of the
 * resource is obtained from the FormBindingConfig.resource$.
 *     </li>
 *     <li>(not yet supported) advanced binding with the naming pattern
 * "resource.{type}.{id}.{attributes/relationships}.{label}".
 *     It allows to edit multiple resources in the same forFormElement.
 *     </li>
 * <ul>
 *
 * Similarly, JSON API errors are mapped back to forFormElement controls trougth the source pointer of the error. If such a
 * mapping is not found, the error is added to the errors attribute of this class. Usually applications show such errors above
 * all fields in the config.
 *
 * You may also have a look at the ArbExpressionModule. Its ExpressionDirective provides an alternative to NgModel
 * that binds both a value and sets the label of forFormElement control with a single (type-safe) attribute.
 */
export class FormBinding {

	/**
	 * Observable to the resource to be edited. The forFormElement binding is active as long as there is
	 * at least one subscriber to this Observable.
	 */
	public resource$: Observable<StoreResource>;

	/**
	 * Contains all errors that cannot be assigned to a forFormElement control. Usually such errors are shown on top above
	 * all controls.
	 */
	public errors: Array<ResourceError> = [];

	/**
	 * the forFormElement also sends out valueChanges upon initialization, we do not want that and filter them out with this flag
	 */
	private wasDirty = false;

	/**
	 * id of the main resource to be edited.
	 */
	private primaryResourceId: ResourceIdentifier = null;

	/**
	 * Subscription to forFormElement changes. Gets automatically cancelled if there are no subscriptions anymore to
	 * resource$.
	 */
	private formSubscription: Subscription = null;

	constructor(private ngrxJsonApiService: NgrxJsonApiService, private config: FormBindingConfig,
		private operationsService: OperationsService) {

		if (this.config.form === null) {
			throw new Error('no forFormElement provided');
		}
		if (this.config.queryId === null) {
			throw new Error('no queryId provided');
		}

		// we make use of share() to keep the this.config.resource$ subscription
		// as long as there is at least subscriber on this.resource$.
		this.resource$ = this.ngrxJsonApiService.selectOneResults(this.config.queryId, true)
			.filter(it => !it.loading)
			.map(it => it.data as StoreResource)
			.filter(it => !_.isEmpty(it)) // ignore deletions
			.distinctUntilChanged(function (a, b) {
				return _.isEqual(a, b);
			})
			.do(resource => this.checkFormSubscription())
			.do(resource => this.primaryResourceId = {type: resource.type, id: resource.id})
			.do(resource => this.updateFormErrorsFromStoreUpdates(resource))
			.finally(() => this.cancelFormSubscription)
			.share();
	}

	protected cancelFormSubscription() {
		if (this.formSubscription !== null) {
			this.formSubscription.unsubscribe();
			this.formSubscription = null;
		}
	}

	protected checkFormSubscription() {
		if (this.formSubscription === null) {
			// update store from value changes, for more information see
			// https://embed.plnkr.co/9aNuw6DG9VM4X8vUtkAa?show=app%2Fapp.components.ts,preview
			const formChanges$ = this.config.form.statusChanges
				.filter(valid => valid === 'VALID')
				.withLatestFrom(this.config.form.valueChanges, (valid, values) => values)
				.filter(it => this.config.form.dirty || this.wasDirty)
				.debounceTime(100)
				.distinctUntilChanged(function (a, b) {
					return _.isEqual(a, b);
				})
				.do(it => this.wasDirty = true);
			this.formSubscription = formChanges$.subscribe(formValues => this.updateStoreFromFormValues(formValues));
		}
	}

	protected updateFormErrorsFromStoreUpdates(resource: StoreResource) {
		const errors = resource.errors;
		const newResourceErrors = [];
		if (errors) {
			for (const error  of errors) {
				let control = null;
				if (error.source && error.source.pointer && (error.code || error.id) &&
					error.source.pointer.startsWith('data/')) {
					const sourcePointer = error.source.pointer;
					const basicFormName = this.toBasicFormName(sourcePointer);
					control = this.config.form.controls[basicFormName];
					if (!control) {
						const resourceFormName = this.toResourceFormName(resource, basicFormName);
						control = this.config.form.controls[resourceFormName];
					}
				}
				if (control !== null) {
					let key = error.id;
					if (!key) {
						key = error.code;
					}
					const controlErrors = {};
					controlErrors[key] = error;
					control.setErrors(controlErrors);
				}
				else {
					newResourceErrors.push(error);
				}
			}
		}
		this.errors = newResourceErrors;
	}

	protected toResourceFormName(resource: StoreResource, basicFormName: string) {
		return '/' + resource.type + '/' + resource.id + '/' + basicFormName;
	}

	protected toBasicFormName(sourcePointer: string) {
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

	public save() {
		// TODO Collect resources to update
		if (this.operationsService) {
			// transactional update of multple resources
			this.operationsService.apply();
		}
		else {
			this.ngrxJsonApiService.apply();
		}
	}

	public delete() {
		this.ngrxJsonApiService.deleteResource({
				resourceId: this.primaryResourceId,
				toRemote: true
			}
		);
	}

	public updateStoreFromFormValues(values: any) {
		const patchedResourceMap: {[id: string]: Resource} = {};
		for (const formName of Object.keys(values)) {
			const value = values[formName];

			let type, id, path;
			if (formName.startsWith('/')) {
				[type, id, path] = formName.substring(1).split('/');
			}
			else {
				type = this.primaryResourceId.type;
				id = this.primaryResourceId.id;
				path = formName;
			}
			if (path.startsWith('attributes.') || path.startsWith('relationships.')) {
				const key = type + '_' + id;
				let patchedResource = patchedResourceMap[key];
				if (!patchedResource) {
					patchedResource = {
						id: id,
						type: type,
						attributes: {}
					};
					patchedResourceMap[key] = patchedResource;
				}

				_.set(patchedResource, path, value);
			}
		}

		const patchedResources = _.values(patchedResourceMap);
		for (const patchedResource of patchedResources) {
			const cleanedPatchedResource = this.clearPrimeNgMarkers(patchedResource);
			this.ngrxJsonApiService.patchResource({resource: cleanedPatchedResource});
		}
	}

	/**
	 * Prime NG has to annoying habit of adding _$visited. Cleaned up here. Needs to be further investigated
	 * and preferably avoided.
	 *
	 * FIXME move to HTTP layer or fix PrimeNG, preferably the later.
	 */
	private clearPrimeNgMarkers(resource: Resource) {
		const cleanedResource = _.cloneDeep(resource);
		if (cleanedResource.attributes) {
			for (const attributeName of Object.keys(cleanedResource.attributes)) {
				const value = cleanedResource.attributes[attributeName];
				if (_.isObject(value)) {
					delete value['_$visited'];
				}
			}
		}
		if (cleanedResource.relationships) {
			for (const relationshipName of Object.keys(cleanedResource.relationships)) {
				const relationship = cleanedResource.relationships[relationshipName];
				if (relationship.data) {
					const dependencyIds: Array<ResourceIdentifier> = relationship.data instanceof Array ? relationship.data :
						[relationship.data];
					for (const dependencyId of dependencyIds) {
						delete dependencyId['_$visited'];
					}
				}
			}
		}
		return cleanedResource;
	}
}

