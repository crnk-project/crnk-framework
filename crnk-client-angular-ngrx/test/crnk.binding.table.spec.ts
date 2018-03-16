import { async, ComponentFixture, fakeAsync, getTestBed, TestBed, tick } from '@angular/core/testing';

import 'rxjs/add/operator/merge';
import { Store } from '@ngrx/store';
import { LocalQueryInitAction, NewStoreResourceAction, NgrxJsonApiService, NgrxJsonApiStore, QueryParams } from 'ngrx-json-api';
import { By } from '@angular/platform-browser';
import { TestingModule } from './crnk.test.module';
import { TestTableComponent } from './crnk.test.table.component';
import { DataTableModule } from 'primeng/primeng';
import { Observable } from 'rxjs/Observable';


describe('TableBinding', () => {
	let store: Store<any>;
	let jsonapi: NgrxJsonApiService;

	let fixture: ComponentFixture<TestTableComponent>;

	beforeEach(async(() => {
		return TestBed.configureTestingModule({
			imports: [TestingModule, DataTableModule],
			declarations: [TestTableComponent]
		}).compileComponents();

	}));


	beforeEach(() => {
		store = getTestBed().get(Store);
		jsonapi = getTestBed().get(NgrxJsonApiService);
		fixture = TestBed.createComponent(TestTableComponent);
	});

	beforeEach(() => {
		store.dispatch(new NewStoreResourceAction({
			id: 'someBean.someAttribute1',
			type: 'meta/attribute',
			attributes: {
				name: 'SomeAttribute1'
			}
		}, 'default'));
		store.dispatch(new NewStoreResourceAction({
			id: 'someBean.someAttribute2',
			type: 'meta/attribute',
			attributes: {
				name: 'SomeAttribute2'
			}
		}, 'default'));
		store.dispatch(new LocalQueryInitAction({
			queryId: 'tableQuery',
			type: 'meta/attribute'
		}, 'default'))
	});

	it('should sync with store', fakeAsync(() => {
		fixture.detectChanges();
		tick();

		let cellElements = fixture.debugElement.queryAll(By.css('.ui-cell-data'));
		expect(cellElements.length).toEqual(2);
		expect(cellElements[0].nativeElement.textContent).toEqual('SomeAttribute1');
		expect(cellElements[1].nativeElement.textContent).toEqual('SomeAttribute2');

		let filterElement = fixture.debugElement.query(By.css('.ui-column-filter'));
		filterElement.nativeElement.value = 'SomeAttribute1';
		filterElement.nativeElement.dispatchEvent(new Event('input'));
		fixture.detectChanges();
		tick(500);

		const snapshot = jsonapi['storeSnapshot'] as NgrxJsonApiStore;
		const querySnapshot = snapshot.queries['tableQuery'];
		expect(querySnapshot.query.params.offset).toEqual(0);
		expect(querySnapshot.query.params.limit).toEqual(10);
		expect(querySnapshot.query.params.filtering.length).toEqual(1);
		expect(querySnapshot.query.params.filtering[0].path).toEqual('name');
		expect(querySnapshot.query.params.filtering[0].operator).toEqual('exact');
		expect(querySnapshot.query.params.filtering[0].value).toEqual('SomeAttribute1');

		// TODO ngrx-json-api broken
		// cellElements = fixture.debugElement.queryAll(By.css(".ui-cell-data"));
		// expect(cellElements.length).toEqual(1);
		// expect(cellElements[0].nativeElement.textContent).toEqual("SomeAttribute1");

	}));

	it('should apply custom query params', fakeAsync(() => {
		const config = fixture.componentInstance.config;
		const customQueryParams: QueryParams = {
			fields: ['testField']
		}
		config.customQueryParams = Observable.of(customQueryParams)

		fixture.detectChanges();
		tick();

		// check custom params applied from start
		let snapshot = jsonapi['storeSnapshot'] as NgrxJsonApiStore;
		let querySnapshot = snapshot.queries['tableQuery'];
		let appliedParams = querySnapshot.query.params;
		expect(appliedParams.offset).toEqual(0);
		expect(appliedParams.limit).toEqual(10);
		expect(appliedParams.fields.length).toEqual(1);
		expect(appliedParams.fields[0]).toEqual('testField');
		expect(appliedParams.filtering).toBeUndefined();

		let filterElement = fixture.debugElement.query(By.css('.ui-column-filter'));
		filterElement.nativeElement.value = 'SomeAttribute1';
		filterElement.nativeElement.dispatchEvent(new Event('input'));
		fixture.detectChanges();
		tick(500);

		// check custom params with table params
		snapshot = jsonapi['storeSnapshot'] as NgrxJsonApiStore;
		querySnapshot = snapshot.queries['tableQuery'];
		appliedParams = querySnapshot.query.params;
		expect(appliedParams.offset).toEqual(0);
		expect(appliedParams.limit).toEqual(10);
		expect(appliedParams.fields.length).toEqual(1);
		expect(appliedParams.fields[0]).toEqual('testField');
		expect(appliedParams.filtering.length).toEqual(1);
		expect(appliedParams.filtering[0].path).toEqual('name');
		expect(appliedParams.filtering[0].operator).toEqual('exact');
		expect(appliedParams.filtering[0].value).toEqual('SomeAttribute1');
	}));
});
