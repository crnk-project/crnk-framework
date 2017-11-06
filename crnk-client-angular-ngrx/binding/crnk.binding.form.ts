import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
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
import {
	getNgrxJsonApiZone,
	NGRX_JSON_API_DEFAULT_ZONE,
	NgrxJsonApiService,
	NgrxJsonApiStore,
	NgrxJsonApiStoreData, NgrxJsonApiZoneService,
	Resource,
	ResourceError,
	ResourceIdentifier,
	StoreResource
} from 'ngrx-json-api';
import {Store} from "@ngrx/store";
import {getNgrxJsonApiStore$, getStoreData$} from './crnk.binding.utils';
import {ReplaySubject} from "rxjs/ReplaySubject";


interface ResourceFieldRef {
	resourceId: ResourceIdentifier;
	path: String;
}

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

	/**
	 * By default a denormalized selectOneResults is used to fetch resources. Any update of those
	 * resources triggers an update of the FormControl states. Set this flag to true to listen to all store changes.
	 */
	mapNonResultResources?: boolean;

	/**
	 * Zone to use within ngrx-json-api.
	 */
	zoneId?: string;
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
 * You may also have a look at the CrnkExpressionModule. Its ExpressionDirective provides an alternative to NgModel
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
	public unmappedErrors: Array<ResourceError> = [];

	/**
	 * the forFormElement also sends out valueChanges upon initialization, we do not want that and filter them out with this flag
	 */
	private wasDirty = false;

	/**
	 * id of the main resource to be edited.
	 */
	private primaryResourceId: ResourceIdentifier = null;

	/**
	 * list of resources edited by this binding. May include related resources next to the primary one.
	 */
	private editResourceIds: { [key: string]: ResourceIdentifier } = {};

	/**
	 * Subscription to forFormElement changes. Gets automatically cancelled if there are no subscriptions anymore to
	 * resource$.
	 */
	private formSubscription: Subscription = null;

	private storeSubscription: Subscription = null;

	private formControlsInitialized = false;

	private _storeDataSnapshot?: NgrxJsonApiStoreData = null;

	private validSubject = new ReplaySubject<boolean>(1);

	private dirtySubject = new ReplaySubject<boolean>(1);

	public valid: Observable<boolean>;

	public dirty: Observable<boolean>;

	private ngrxJsonApiZone: NgrxJsonApiZoneService;

	constructor(ngrxJsonApiService: NgrxJsonApiService, private config: FormBindingConfig,
				private store: Store<any>) {

		const zoneId = config.zoneId || NGRX_JSON_API_DEFAULT_ZONE;
		this.ngrxJsonApiZone = ngrxJsonApiService.getZone(zoneId)

		this.dirtySubject.next(false);
		this.validSubject.next(true);

		this.dirty = this.dirtySubject.asObservable().distinctUntilChanged();
		this.valid = this.validSubject.asObservable().distinctUntilChanged();

		if (this.config.form === null) {
			throw new Error('no forFormElement provided');
		}
		if (this.config.queryId === null) {
			throw new Error('no queryId provided');
		}

		// we make use of share() to keep the this.config.resource$ subscription
		// as long as there is at least subscriber on this.resource$.
		this.resource$ = this.ngrxJsonApiZone.selectOneResults(this.config.queryId, true)
			.filter(it => !_.isEmpty(it)) // ignore deletions
			.filter(it => !it.loading)
			.map(it => it.data as StoreResource)
			.filter(it => !_.isEmpty(it)) // ignore deletions
			.distinctUntilChanged(function (a, b) {
				return _.isEqual(a, b);
			})
			.do(() => this.checkSubscriptions())
			.do(resource => this.primaryResourceId = {type: resource.type, id: resource.id})
			.withLatestFrom(this.store, (resource, store) => {
				let jsonapiState = getNgrxJsonApiZone(store, zoneId);
				this._storeDataSnapshot = jsonapiState.data;
				this.mapResourceToControlErrors(jsonapiState.data);
				this.updateDirtyState(jsonapiState.data);
				return resource;
			})
			.finally(() => this.cancelSubscriptions)
			.share();

	}

	protected cancelSubscriptions() {
		if (this.formSubscription !== null) {
			this.formSubscription.unsubscribe();
			this.formSubscription = null;
		}
		if (this.storeSubscription !== null) {
			this.storeSubscription.unsubscribe();
			this.storeSubscription = null;
		}
	}

	protected checkSubscriptions() {
		if (this.formSubscription === null) {
			// update store from value changes, for more information see
			// https://embed.plnkr.co/9aNuw6DG9VM4X8vUtkAa?show=app%2Fapp.components.ts,preview
			const formChanges$ = this.config.form.statusChanges
				.do(valid => this.validSubject.next(valid === 'VALID'))
				.filter(valid => valid === 'VALID')
				.do(() => {
					// it may take a moment for a form with all controls to initialize and register.
					// there seems no proper Angular lifecycle for this to check(???). Till no
					// control is found, we perform the mapping also here.
					//
					// geting notified about new control would be great...
					if (!this.formControlsInitialized) {
						this.mapResourceToControlErrors(this._storeDataSnapshot);
					}
				})

				.withLatestFrom(this.config.form.valueChanges, (valid, values) => values)
				.filter(it => this.config.form.dirty || this.wasDirty)
				.debounceTime(20)
				.distinctUntilChanged(function (a, b) {
					return _.isEqual(a, b);
				})
				.do(it => this.wasDirty = true);
			this.formSubscription = formChanges$.subscribe(formValues => this.updateStoreFromFormValues(formValues));
		}

		if (this.storeSubscription != null && this.config.mapNonResultResources) {
			this.storeSubscription = this.store
				.let(getNgrxJsonApiStore$)
				.let(getStoreData$)
				.subscribe(data => {
					this.mapResourceToControlErrors(data);
				});
		}
	}

	protected mapResourceToControlErrors(data: NgrxJsonApiStoreData) {

		const form = this.config.form;
		if (this.primaryResourceId) {
			const primaryResource = data[this.primaryResourceId.type][this.primaryResourceId.id];

			const newUnmappedErrors = [];
			for (const resourceError of primaryResource.errors) {

				let mapped = false;
				if (resourceError.source && resourceError.source.pointer) {
					const path = this.toPath(resourceError.source.pointer);
					const formName = this.toResourceFormName(primaryResource, path);
					if (form.controls[formName] || form.controls[path]) {
						mapped = true;
					}
				}
				if (!mapped) {
					newUnmappedErrors.push(resourceError);
				}
			}
			this.unmappedErrors = newUnmappedErrors;
		}
	}

	protected updateDirtyState(data: NgrxJsonApiStoreData) {
		function isDirty(resourceId: ResourceIdentifier) {
			const resource = data[resourceId.type][resourceId.id];
			return resource && resource.state !== 'IN_SYNC';
		}

		var newDirty = isDirty(this.primaryResourceId);
		for (const editedResourceId of _.values(this.editResourceIds)) {
			newDirty = newDirty || isDirty(editedResourceId)
		}
		this.dirtySubject.next(newDirty);
	}

	protected toResourceFormName(resource: StoreResource, basicFormName: string) {
		return '//' + resource.type + '//' + resource.id + '//' + basicFormName;
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

	public save() {
		this.ngrxJsonApiZone.apply();
	}

	public delete() {
		this.ngrxJsonApiZone.deleteResource({
				resourceId: this.primaryResourceId,
				toRemote: true
			}
		);
	}

	/**
	 * computes type, id and field path from formName.
	 */
	private parseResourceFieldRef(formName: string): ResourceFieldRef {
		if (formName.startsWith('//')) {
			const [type, id, path] = formName.substring(2).split('//');
			return {
				resourceId: {
					type: type,
					id: id
				},
				path: path
			};
		}
		else {
			return {
				resourceId: {
					type: this.primaryResourceId.type,
					id: this.primaryResourceId.id
				},
				path: formName
			};
		}
	}

	public updateStoreFromFormValues(values: any) {
		const patchedResourceMap: { [id: string]: Resource } = {};
		for (const formName of Object.keys(values)) {
			const value = values[formName];

			const formRef = this.parseResourceFieldRef(formName);
			if (formRef.path.startsWith('attributes.') || formRef.path.startsWith('relationships.')) {
				const key = formRef.resourceId.type + '_' + formRef.resourceId.id;

				const storeTypeSnapshot = this._storeDataSnapshot[formRef.resourceId.type];
				const storeResourceSnapshot = storeTypeSnapshot ? storeTypeSnapshot[formRef.resourceId.id] : undefined;
				const storeValueSnapshot = storeResourceSnapshot ? _.get(storeResourceSnapshot, formRef.path) : undefined;
				if (!_.isEqual(storeValueSnapshot, value)) {
					let patchedResource = patchedResourceMap[key];
					if (!patchedResource) {
						patchedResource = {
							id: formRef.resourceId.id,
							type: formRef.resourceId.type,
							attributes: {}
						};
						patchedResourceMap[key] = patchedResource;

						const resourceKey = formRef.resourceId.id + "@" + formRef.resourceId.type;
						if (!this.editResourceIds[resourceKey]) {
							this.editResourceIds[resourceKey] = formRef.resourceId;
						}
					}
					_.set(patchedResource, formRef.path, value);

				}
			}
		}

		const patchedResources = _.values(patchedResourceMap);
		for (const patchedResource of patchedResources) {
			const cleanedPatchedResource = this.clearPrimeNgMarkers(patchedResource);
			this.ngrxJsonApiZone.patchResource({resource: cleanedPatchedResource});
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

