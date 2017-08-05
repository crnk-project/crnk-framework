import {StoreResource, NgrxJsonApiStore} from "ngrx-json-api";

export function getPendingChanges(state: NgrxJsonApiStore): Array<StoreResource> {
	const pending: Array<StoreResource> = [];
	Object.keys(state.data).forEach(type => {
		Object.keys(state.data[type]).forEach(id => {
			const storeResource = state.data[type][id];
			if ((storeResource.state !== 'IN_SYNC') && (storeResource.state !== 'NEW')) {
				pending.push(storeResource);
			}
		});
	});
	return pending;
}

