import {Injectable} from "@angular/core";
import {Store} from "@ngrx/store";
import {OperationsInitAction} from "./crnk.operations.actions";

@Injectable()
export class OperationsService {

	constructor(private store: Store<any>) {
	}

	public apply() {
		this.store.dispatch(new OperationsInitAction());
	}
}
