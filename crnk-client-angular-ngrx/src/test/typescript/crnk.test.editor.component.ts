import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {FormBinding} from "../../main/typescript/binding/crnk.binding.form";
import {QMetaAttribute} from "../../main/typescript/meta/meta.attribute";
import {Subscription} from "rxjs/Subscription";
import {CrnkBindingService} from "../../main/typescript/binding/crnk.binding.service";
import {BeanBinding} from "../../main/typescript/expression/crnk.expression";

@Component({
	selector: 'test-editor',
	templateUrl: "crnk.test.editor.component.html"
})
export class TestEditorComponent implements OnInit, OnDestroy {

	@ViewChild('formRef') form;

	public binding: FormBinding;

	public resource: QMetaAttribute;

	private subscription: Subscription;

	constructor(private bindingService: CrnkBindingService) {
	}

	ngOnInit() {
		this.binding = this.bindingService.bindForm({
			form: this.form,
			queryId: 'editorQuery'
		});

		// note that one could use the "async" pipe and "as" operator, but so
		// far code completion does not seem to work in Intellij. For this reason
		// the example sticks to slightly more verbose subscriptions.
		this.subscription = this.binding.resource$.subscribe(
			person => {
				this.resource = new QMetaAttribute(new BeanBinding(person), null);
			}
		);
	}

	ngOnDestroy() {
		this.subscription.unsubscribe();
	}
}
