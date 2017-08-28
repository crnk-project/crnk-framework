import {async, ComponentFixture, fakeAsync, getTestBed, TestBed, tick} from '@angular/core/testing';

import "rxjs/add/operator/merge";
import {Store} from '@ngrx/store';
import {LocalQueryInitAction, ModifyStoreResourceErrorsAction, NewStoreResourceAction} from "ngrx-json-api";
import {By} from "@angular/platform-browser";
import {TestEditorComponent} from "./crnk.test.editor.component";
import {TestingModule} from "./crnk.test.module";


describe('FormBinding', () => {
	let store: Store<any>;

	let fixture: ComponentFixture<TestEditorComponent>;

	beforeEach(async(() => {
		return TestBed.configureTestingModule({
			imports: [TestingModule],
			declarations: [TestEditorComponent]
		}).compileComponents();

	}));


	beforeEach(() => {
		store = getTestBed().get(Store);
		fixture = TestBed.createComponent(TestEditorComponent);
	});

	beforeEach(() => {
		store.dispatch(new NewStoreResourceAction({
			id: 'someBean.someAttribute',
			type: 'meta/attribute',
			attributes: {
				name: 'SomeAttribute'
			}
		}))
		store.dispatch(new LocalQueryInitAction({
			queryId: 'editorQuery',
			type: 'meta/attribute',
			id: 'someBean.someAttribute',
		}))
	});

	it('should sync with store', fakeAsync(() => {
		fixture.detectChanges();
		tick();

		let nameInputElement = fixture.debugElement.query(By.css("#nameInput"));
		expect(fixture.componentInstance.resource.attributes.name.getValue()).toEqual("SomeAttribute");
		expect(nameInputElement.nativeElement.value).toEqual("SomeAttribute");
		const initialResource = fixture.componentInstance.resource;

		// make a change
		nameInputElement.nativeElement.value = "UpdatedAttribute";
		nameInputElement.nativeElement.dispatchEvent(new Event('input'));
		fixture.detectChanges();
		tick(200);

		expect(initialResource.attributes.name.getValue()).toEqual("SomeAttribute");
		expect(fixture.componentInstance.resource.attributes.name.getValue()).toEqual("UpdatedAttribute");
		expect(fixture.componentInstance.resource).not.toBe(initialResource);


	}));

	it('should sync JSON API errors to FormControl errors', fakeAsync(() => {

		store.dispatch(new ModifyStoreResourceErrorsAction({
			resourceId: {
				type: 'meta/attribute',
				id: 'someBean.someAttribute',
			},
			modificationType: 'SET',
			errors: [
				{
					code: 'someCode',
					detail: "someError",
					source: {
						pointer: '/data/attributes/name'
					}
				}
			]
		}))


		fixture.detectChanges();
		tick(100);
		fixture.detectChanges();
		tick(100);

		let nameInputElement = fixture.debugElement.query(By.css("#nameInput"));
		expect(fixture.componentInstance.resource.attributes.name.getValue()).toEqual("SomeAttribute");
		expect(nameInputElement.nativeElement.value).toEqual("SomeAttribute");

		let resourceErrorElements = fixture.debugElement.queryAll(By.css("#resourceError"));
		expect(resourceErrorElements.length).toEqual(1);
		expect(resourceErrorElements[0].nativeElement.textContent).toEqual("someError");

		let controlErrorElements = fixture.debugElement.queryAll(By.css("#controlError"));
		expect(controlErrorElements.length).toEqual(1);
		expect(controlErrorElements[0].nativeElement.textContent).toEqual("jsonapi.someCode");

		// make a change to clear the errors in the store
		nameInputElement.nativeElement.value = "UpdatedAttribute";
		nameInputElement.nativeElement.dispatchEvent(new Event('input'));
		fixture.detectChanges();
		tick(200);
		fixture.detectChanges();
		tick(200);
		fixture.detectChanges();
		tick(200);

		resourceErrorElements = fixture.debugElement.queryAll(By.css("#resourceError"));
		expect(resourceErrorElements.length).toEqual(0);
		controlErrorElements = fixture.debugElement.queryAll(By.css("#controlError"));
		expect(controlErrorElements.length).toEqual(0);

		// make an illegal change that triggers the required validation of the FormControl
		nameInputElement.nativeElement.value = ""; // => illegal
		nameInputElement.nativeElement.dispatchEvent(new Event('input'));
		fixture.detectChanges();
		tick(200);
		fixture.detectChanges();
		tick(200);
		fixture.detectChanges();
		tick(200);

		// gets detected on a control-level, but not on a resource-level.
		resourceErrorElements = fixture.debugElement.queryAll(By.css("#resourceError"));
		expect(resourceErrorElements.length).toEqual(0);
		controlErrorElements = fixture.debugElement.queryAll(By.css("#controlError"));
		expect(controlErrorElements.length).toEqual(1);


	}));
});
