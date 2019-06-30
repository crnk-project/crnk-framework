import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Facet} from '~/facet';
import {FacetValue} from '~/facet.value';
import * as _ from 'lodash';
import {Subject} from 'rxjs';
import {debounceTime} from 'rxjs/operators';

@Component({
	selector: 'sb4b-facet',
	templateUrl: './common-facet.component.html',
	styleUrls: ['common-facet.component.css'],
})
export class CommonFacetComponent {

	private _facet: Facet;

	@Input()
	public selection: string[] = [];

	public values: FacetValue[];

	@Output()
	public selectionChange = new EventEmitter();

	// wait with events a short while to account for loading, multi-selections, etc. => avoids loops
	private selectionDebouncer = new Subject();

	constructor() {
		this.selectionDebouncer
			.pipe(debounceTime(150))
			.subscribe(value => this.selectionChange.emit(value));
	}

	public get facet() {
		return this._facet;
	}

	@Input()
	public set facet(facet: Facet) {
		this._facet = facet;

		this.values = facet.labels.map(label => facet.values[label]);
	}

	public updateFacet(selectedLabels) {
		// avoid repeated request due to ordering changes => set of labels important, not order
		const sortedSelection = _.sortBy(selectedLabels);
		if (!_.isEqual(this.selection, sortedSelection)) {
			this.selection = sortedSelection;
			this.selectionDebouncer.next(sortedSelection);
			return this.values;
		}
	}
}

