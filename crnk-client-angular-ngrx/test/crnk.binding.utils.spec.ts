///<reference path="../binding/crnk.binding.utils.ts"/>
import 'rxjs/add/operator/merge';
import { Query, QueryParams } from 'ngrx-json-api';
import { applyQueryParams } from '../binding';
import { Direction } from 'ngrx-json-api/src/interfaces';

describe('applyQueryParams', () => {

	it('overridePaging', () => {
		let query: Query = {
			queryId: 'test',
			params: {
				filtering: [],
				sorting: [],
				include: [],
				fields: [],
				offset: 0,
				limit: 12
			}
		};

		let params: QueryParams = {
			filtering: [],
			sorting: [],
			include: [],
			fields: [],
			offset: 12,
			limit: 13
		};

		applyQueryParams(query, params);

		expect(query.params.offset).toBe(12);
		expect(query.params.limit).toBe(13);
	});

	it('overrideSorting', () => {
		let query: Query = {
			queryId: 'test',
			params: {
				sorting: [{ api: 'test', direction: Direction.DESC }]
			}
		};

		let params: QueryParams = {
			sorting: [
				{ api: 'a', direction: Direction.DESC },
				{ api: 'b', direction: Direction.ASC }
			]
		};

		applyQueryParams(query, params);

		expect(query.params.sorting.length).toBe(2);
		expect(query.params.sorting).toEqual([
			{ api: 'a', direction: Direction.DESC },
			{ api: 'b', direction: Direction.ASC }
		]);
		expect(query.params.sorting === params.sorting).toBeFalsy("must create copy");
	});

	it('unionInclusion', () => {
		let query: Query = {
			queryId: 'test',
			params: {
				include: ['a', 'b'],
			}
		};

		let params: QueryParams = {
			include: ['b', 'c'],
		};

		applyQueryParams(query, params);

		expect(query.params.include.length).toBe(3);
		expect(query.params.include).toEqual(['a', 'b', 'c']);
	});

	it('unionFields', () => {
		let query: Query = {
			queryId: 'test',
			params: {
				fields: ['a', 'b'],
			}
		};

		let params: QueryParams = {
			fields: ['b', 'c'],
		};

		applyQueryParams(query, params);

		expect(query.params.fields.length).toBe(3);
		expect(query.params.fields).toEqual(['a', 'b', 'c']);
	});


	it('unionFilters', () => {
		let query: Query = {
			queryId: 'test',
			params: {
				filtering: [
					{path: 'attr1', operator: 'EQ', value: 'val1'},
					{path: 'attr2', operator: 'EQ', value: 'val2'},
				]
			}
		};

		let params: QueryParams = {
			filtering: [
				{path: 'attr2', operator: 'EQ', value: 'updated2'},
				{path: 'attr3', operator: 'EQ', value: 'updated3'},
			]
		};

		applyQueryParams(query, params);

		expect(query.params.filtering.length).toBe(3);
		expect(query.params.filtering).toEqual([
			{path: 'attr1', operator: 'EQ', value: 'val1'},
			{path: 'attr2', operator: 'EQ', value: 'updated2'},
			{path: 'attr3', operator: 'EQ', value: 'updated3'}
		]);
	});
});