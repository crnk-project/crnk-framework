import 'rxjs/add/operator/merge';


import {MetaAttribute, QMetaAttribute} from '../meta/meta.attribute';
import {BeanBinding} from '../expression/crnk.expression';
import {ArrayPath, MapPath, NumberPath, StringPath} from "../expression";

it('ArrayPath accessor', () => {
	const arrayBean = {
		type: 'person',
		id: '1',
		test: [1, 2, 3]
	};
	const qbean = new BeanBinding(arrayBean);

	const arrayPath = new ArrayPath(qbean, "test", NumberPath);
	expect(arrayPath.getResource()).toBe(arrayBean);
	expect(arrayPath.getValue()).toBe(arrayBean.test);
	expect(arrayPath.getSourcePointer()).toBe('/data/test');
	expect(arrayPath.isConstant()).toBeFalsy();
	expect(arrayPath.isPath()).toBeTruthy();

	const element1 = arrayPath.getElement(1);
	expect(element1.getResource()).toBe(arrayBean);
	expect(element1.getValue()).toBe(arrayBean.test[1]);
	expect(element1.getSourcePointer()).toBe('/data/test[1]');
	expect(element1.toFormName()).toBe('//person//1//test[1]');
	expect(element1.isConstant()).toBeFalsy();
	expect(element1.isPath()).toBeTruthy();
});

it('MapPath accessor', () => {
	const mapData = {
		type: 'person',
		id: '1',
		test: {
			'a': 1,
			'b': 2
		}
	};
	const qbean = new BeanBinding(mapData);

	const mapPath = new MapPath(qbean, "test", NumberPath);
	expect(mapPath.getResource()).toBe(mapData);
	expect(mapPath.getValue()).toBe(mapData.test);
	expect(mapPath.getSourcePointer()).toBe('/data/test');
	expect(mapPath.isConstant()).toBeFalsy();
	expect(mapPath.isPath()).toBeTruthy();

	const element1 = mapPath.getElement('a');
	expect(element1.getResource()).toBe(mapData);
	expect(element1.getValue()).toBe(mapData.test['a']);
	expect(element1.getSourcePointer()).toBe('/data/test[a]');
	expect(element1.toFormName()).toBe('//person//1//test[a]');
	expect(element1.isConstant()).toBeFalsy();
	expect(element1.isPath()).toBeTruthy();
});

describe('BeanPath accessor', () => {

	// tag::docs[]
	let bean: MetaAttribute;
	let qbean: QMetaAttribute;

	beforeEach(() => {
		bean = {
			id: 'someBean.title',
			type: 'meta/attribute',
			attributes: {
				name: 'someName'
			},
			relationships: {
				type: {
					data: {type: 'testType', id: 'testId'},
					reference: {type: 'testType', id: 'testId', attributes: {name: 'testName'}},
				}
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
		expect(qbean.relationships.type.data.id.getValue()).toBe('testId');
		expect(qbean.relationships.type.data.type.getValue()).toBe('testType');
		expect(qbean.relationships.type.reference.attributes.name.getValue()).toBe('testName');
		expect(qbean.relationships.type.reference.attributes.name.toQueryPath()).toBe('type.name');
	});

	it('should update bean', () => {
		qbean.attributes.name.setValue('updatedName');
		expect(bean.attributes.name).toEqual('updatedName');
	});

	it('should provide form name', () => {
		expect(qbean.attributes.name.toFormName()).toEqual('//meta/attribute//someBean.title//attributes.name');
	});
	// end::docs[]
});
