import * as _ from 'lodash';


export function extractType(it) {
	if (it.charAt(0) === '[') {
		const sep = it.indexOf(']');
		return {
			type: it.substr(1, sep - 1),
			path: it.substr(sep + 1)
		};
	}
	else {
		return {
			type: '',
			path: it
		};
	}
}

function getType(it) {
	return it.type;
}

function getPath(it) {
	return it.path;
}

function getUrlPath(paramName, it) {
	return paramName + (it[0] !== '' ? '[' + it[0] + ']' : '') + '=' + it[1].map(getPath).join(',');
}

function generateCrnkQueryParams(paramName, included: Array<string>) {
	if (_.isEmpty(included)) {
		return '';
	}
	const includeGroups = _.toPairs(_.groupBy(included.map(extractType), getType));
	return includeGroups.map(it => getUrlPath(paramName, it)).join('&');
}

export function generateCrnkIncludedQueryParams(included: Array<string>): string {
	return generateCrnkQueryParams('include', included);
}

export function generateCrnkFieldsQueryParams(fields: Array<string>): string {
	return generateCrnkQueryParams('fields', fields);
}


export const CRNK_URL_BUILDER = {
	generateIncludedQueryParams: generateCrnkIncludedQueryParams,
	generateFieldsQueryParams: generateCrnkFieldsQueryParams,
};

/* TODO
 generateFilteringQueryParams: generateCrnkFilteringQueryParams,
 generateFilteringQueryParams?: (params: Array<FilteringParam>) => string;
 generateSortingQueryParams?: (params: Array<SortingParam>) => string;
 generateQueryParams?: (...params: Array<string>) => string;
 */
