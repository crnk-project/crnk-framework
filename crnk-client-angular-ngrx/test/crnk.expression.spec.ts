import "rxjs/add/operator/merge";


import {MetaAttribute, QMetaAttribute} from "../meta/meta.attribute";
import {BeanBinding} from "../expression/crnk.expression";

describe('Expression', () => {

	// tag::docs[]
	let bean: MetaAttribute;
	let qbean: QMetaAttribute;

	beforeEach(() => {
		bean = {
			id: 'someBean.title',
			type: 'meta/attribute',
			attributes: {
				name: 'someName'
			}
		};
		qbean = new QMetaAttribute(new BeanBinding(bean));
	});


	it('should bind to bean', () => {
		expect(qbean.id.getValue()).toEqual('someBean.title');
		expect(qbean.attributes.name.getValue()).toEqual('someName');
		expect(qbean.attributes.name.toString()).toEqual('attributes.name');
		expect(qbean.id.getResource()).toBe(bean);
		expect(qbean.attributes.name.getResource()).toBe(bean);
	});

	it('should update bean', () => {
		qbean.attributes.name.setValue("updatedName");
		expect(bean.attributes.name).toEqual('updatedName');
	});

	it('should provide form name', () => {
		expect(qbean.attributes.name.toFormName()).toEqual('//meta/attribute//someBean.title//attributes.name');
	});
	// end::docs[]
});
