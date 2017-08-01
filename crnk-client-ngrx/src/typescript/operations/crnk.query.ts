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
};
export function getType(it) {
	return it.type;
};
export function getPath(it) {
	return it.path;
};
export function getIncludePath(it) {
	return 'include' + (it[0] !== '' ? '[' + it[0] + ']' : '') + '=' + it[1].map(getPath).join(',');
}

export function generateCrnkIncludedQueryParams(included: Array<string>): string {
	if (_.isEmpty(included)) {
		return '';
	}

	const typedIncludes = included.map(extractType);
	const includeGroups = _.toPairs(_.groupBy(typedIncludes, getType));
	const include = includeGroups.map(getIncludePath).join('&');
	return include;
};


export const ARB_CRNK_URL_BUILDER = {
	generateIncludedQueryParams: generateCrnkIncludedQueryParams,
};

/* TODO
 generateFilteringQueryParams: generateCrnkFilteringQueryParams,
 generateFieldsQueryParams: generateCrnkFieldsQueryParams,
 generateIncludedQueryParams: generateIncludedQueryParams

 generateFilteringQueryParams?: (params: Array<FilteringParam>) => string;
 generateFieldsQueryParams?: (params: Array<string>) => string;
 generateIncludedQueryParams?: (params: Array<string>) => string;
 generateSortingQueryParams?: (params: Array<SortingParam>) => string;
 generateQueryParams?: (...params: Array<string>) => string;
 */
