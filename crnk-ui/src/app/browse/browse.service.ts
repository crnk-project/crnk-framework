import * as _ from "lodash";
import {Component, Injectable, Inject} from "@angular/core";
import {TreeNode} from "primeng/primeng";
import {Http, Response, RequestOptions, Headers} from "@angular/http";
import {LocalStorageService} from "angular-2-local-storage";

import { DOCUMENT } from '@angular/platform-browser';

@Injectable()
export class BrowseUtils {

	public normalize(value: string, separator: string): string {
		if (_.isEmpty(value))
			return '';
		if (value.endsWith(separator))
			return value;
		return value + separator;
	}

	public stripSeparators(value: string): string {
		if (value.startsWith('/'))
			value = value.substring(1, value.length);
		if (value.endsWith('/'))
			value = value.substring(0, value.length - 1);
		return value;
	}


}


const DEFAULT_PREFERENCES = {

	query: {
		type: "",
		id: "",
		relationship: "",
		parameters: ""
	},

	expand: {
		links: true,
		attributes: true,
		relationships: true,
		meta: true
	},

	baseUrl: null,

	documentDisplayType: 'tree'
};


@Injectable()
export class BrowsePreferencesService {


	constructor(private localStorageService: LocalStorageService,
		@Inject(DOCUMENT) private document: any) {

	}

	public loadPreferences() {
		let loaded: any = {
			baseUrl: this.localStorageService.get("baseUrl"),
			query: {
				type: this.localStorageService.get("query.type"),
				id: this.localStorageService.get("query.id"),
				relationship: this.localStorageService.get("query.relationship"),
				relationshipId: this.localStorageService.get("query.relationshipId"),
				parameters: this.localStorageService.get("query.parameters")
			},

			expand: {
				links: this.localStorageService.get("expand.links"),
				meta: this.localStorageService.get("expand.meta"),
				attributes: this.localStorageService.get("expand.attributes"),
				relationships: this.localStorageService.get("expand.relationships"),
			},
			documentDisplayType: this.localStorageService.get("documentDisplayType")
		};

		const href = this.document.location.href;
		const suffix = "browse/#/";
		if(href.endsWith(suffix)){
			loaded.baseUrl = href.substring(0, href.length - suffix.length);
		}

		if(loaded.baseUrl && !loaded.baseUrl.endsWith("/")){
			loaded.baseUrl = loaded.baseUrl + "/";
		}
		if(!loaded.documentDisplayType){
			loaded.documentDisplayType = 'tree';
		}

		return Object.assign({}, DEFAULT_PREFERENCES, loaded);
	}


	public savePreferences(preferences) {
		this.localStorageService.add('baseUrl', preferences.baseUrl);
		this.localStorageService.add('query.type', preferences.query.type);
		this.localStorageService.add('query.id', preferences.query.id);
		this.localStorageService.add('query.relationship', preferences.query.relationship);
		this.localStorageService.add('query.relationshipId', preferences.query.relationshipId);
		this.localStorageService.add('query.parameters', preferences.query.parameters);
		this.localStorageService.add('expand.links', preferences.expand.links);
		this.localStorageService.add('expand.meta', preferences.expand.meta);
		this.localStorageService.add('expand.relationships', preferences.expand.relationships);
		this.localStorageService.add('expand.attributes', preferences.expand.attributes);
		this.localStorageService.add('documentDisplayType', preferences.documentDisplayType);
	}


}

@Injectable()
export class BrowseService {


	public metaElements: any = {};


	constructor(private http: Http) {

	}


	public resolveMeta(data) {
		let element = this.metaElements[data.id];
		if (!element) {
			throw new Error("unable to find " + data.id);
		}
		return element;
	}


	public suggestParameters(metaResource, event) {
		let current = event.query;

		let sep = Math.max(current.indexOf('?'), current.indexOf('&'));
		let eqSep = current.lastIndexOf("=");
		let parameterStartSequence;
		let paramNamePart;
		let paramValuePart;

		if (eqSep > sep) {
			parameterStartSequence = current.substr(0, eqSep + 1);
			paramValuePart = current.substr(eqSep + 1);
			paramNamePart = current.substr(sep + 1, eqSep);
			if (paramNamePart == 'sort' || paramNamePart == 'include' || paramNamePart == 'fields') {
				let valueSep = paramValuePart.lastIndexOf(",");
				if (valueSep != -1) {
					parameterStartSequence = parameterStartSequence + paramValuePart.substr(0, valueSep + 1);
					paramValuePart = paramValuePart.substr(valueSep + 1);
				}
			}
		} else if (sep > eqSep) {
			parameterStartSequence = current.substr(0, sep + 1);
			paramNamePart = current.substr(sep + 1);
			paramValuePart = null;
			eqSep = -1;
		} else {
			parameterStartSequence = "";
			paramNamePart = current;
			paramValuePart == null;
		}

		if (paramValuePart == null && paramNamePart.startsWith("fil") && paramNamePart.indexOf(']') == -1) {
			// filter attribute suggestion
			let attrPart = paramNamePart.startsWith("filter[") ? paramNamePart.substr("filter[".length) : "";
			return this.expandNames(metaResource, paramValuePart)
				.filter(name => name.startsWith(paramValuePart))
				.map(name => parameterStartSequence + "filter[" + name + "]");
		} else if (paramValuePart == null) {
			// default legacy suggestion
			return ['filter[', 'sort', 'page[offset]', 'page[limit]', 'include', 'fields'].filter(it => it.startsWith(paramNamePart));
		} else if (paramValuePart != null && paramNamePart == 'fields') {
			// field suggestions
			return this.expandNames(metaResource, paramValuePart)
				.filter(name => name.startsWith(paramValuePart))
				.map(name => parameterStartSequence + name);
		} else if (paramValuePart != null && paramNamePart == 'include') {
			// include suggestions
			return this.expandNames(metaResource, paramValuePart, (attribute => attribute.attributes.association))
				.filter(name => name.startsWith(paramValuePart))
				.map(name => parameterStartSequence + name);
		} else if (paramValuePart != null && paramNamePart == 'sort') {
			// sort suggestions
			let ascDesc = this.expandNames(metaResource, paramValuePart)
				.filter(name => name.startsWith(paramValuePart))
				.map(name => [name, '-' + name]);
			return _.concat([], ...ascDesc).filter(name => name.startsWith(paramValuePart))
				.map(it => parameterStartSequence + it);
		} else {
			return [];
		}
	}

	private expandNames(rootType, query, predicate?) {
		let pathElements = _.split(query, '.');
		let pathType = rootType;
		let prefix = pathElements.slice(0, pathElements.length - 1).map(it => it + ".").join("");
		for (let pathElement of pathElements.slice(0, pathElements.length - 1)) {
			let matches = pathType.relationships.attributes.data.map(it => this.resolveMeta(it)).filter(attr => attr.attributes.name == pathElement);
			if (matches.length == 1 && matches[0].attributes.association) {
				pathType = this.resolveMeta(matches[0].relationships.type.data);
				pathType = this.resolveMeta(pathType.relationships.elementType.data);
			} else {
				return [];
			}
		}
		if (!predicate) {
			predicate = it => true;
		}

		let attributes = pathType.relationships.attributes.data.map(it => this.resolveMeta(it));
		return attributes.filter(it => predicate(it)).map(it => prefix + it.attributes.name);
	}

}
