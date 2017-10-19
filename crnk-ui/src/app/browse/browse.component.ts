import * as _ from "lodash";
import {TreeNode} from "primeng/primeng";
import {Http, Response, RequestOptions, Headers} from "@angular/http";
import {LocalStorageService} from "angular-2-local-storage";
import {BrowseService, BrowseUtils, BrowsePreferencesService} from "./browse.service";
import { DOCUMENT } from '@angular/platform-browser';
import {Component, Inject} from "@angular/core";


@Component({
	selector: 'browse',  // <home></home>
	templateUrl: 'browse.component.html'
})
export class BrowseComponent {

	public documentNodes: Array<TreeNode>;

	public filteredDocumentNodes: Array<TreeNode> = null;


	public preferences: any;

	public availableTypes: Array<any> = [];

	public availableRelationships: Array<any> = [];

	public documentDisplayTypes: Array<any> = [{label: 'Tree', value: 'tree'}, {label: 'JSON', value: 'json'}];


	public response: Response = null;

	public _documentText: string = null;

	public responseRows: number = 5;

	public metaResource: any;

	public metaResources: any;

	public metaAttributes: Array<any>;

	public httpMethod: string = null;

	private _queryTerm: string = "";

	public editable: boolean = false;

	public documentError: string = null;

	public parameterSuggestions: Array<string> = [];// BASE_PARAMETER_SUGGESTIONS;

	constructor(private http: Http, private localStorageService: LocalStorageService, private service: BrowseService,
		private preferencesService: BrowsePreferencesService, private utils: BrowseUtils,
		@Inject(DOCUMENT) private document: any) {

		this.preferences = this.preferencesService.loadPreferences();

		try {
			this.get();
			this.updateAvailableTypes();
			this.updateAvailableRelationships();
		}
		catch (e) {
			if (console) {
				console.log(e);
			}
		}
	}


	public get expandLinks() {
		return this.preferences.expand.links;
	}

	public set expandLinks(expandLinks) {
		this.preferences.expand.links = expandLinks;
		this.updatedExpandedStates();
	}

	public get expandMeta() {
		return this.preferences.expand.meta;
	}

	public set expandMeta(expandMeta) {
		this.preferences.expand.meta = expandMeta;
		this.updatedExpandedStates();
	}

	public get expandAttributes() {
		return this.preferences.expand.attributes;
	}

	public set expandAttributes(expandAttributes) {
		this.preferences.expand.attributes = expandAttributes;
		this.updatedExpandedStates();
	}

	public get expandRelationships() {
		return this.preferences.expand.relationships;
	}

	public set expandRelationships(expandRelationships) {
		this.preferences.expand.relationships = expandRelationships;
		this.updatedExpandedStates();
	}


	public get documentText() {
		return this._documentText;
	}

	public set documentText(documentText) {
		this._documentText = documentText;
		this.responseRows = this._documentText ? this._documentText.split('\n').length : 5;
		this.documentNodes = null;
		this.filteredDocumentNodes = null;
		this.documentError = null;
	}

	public clearDocumentText() {
		this._documentText = null;
	}

	public get queryTerm() {
		return this._queryTerm;
	}

	public isSingleResource() {
		return !_.isEmpty(this.preferences.query.id);
	}

	public set documentDisplayType(documentDisplayType) {
		this.preferences.documentDisplayType = documentDisplayType;
		this.preferencesService.savePreferences(this.preferences);
		this.syncDocumentJsonText();
	}

	public get documentDisplayType() {
		return this.preferences.documentDisplayType;
	}


	private syncDocumentJsonText() {
		if (this.documentText == null && this.filteredDocumentNodes) {
			this.documentText = this.documentToText(this.filteredDocumentNodes);
		}
		else if (this.documentText != null && !this.filteredDocumentNodes) {
			try {
				let document = JSON.parse(this.documentText);
				this.documentNodes = this.toNodes(document);

				this.search();
			}
			catch (e) {
				this.documentDisplayType = 'text';
				this.documentError = e.message;
			}
		}
	}

	private documentToText(nodes: Array < TreeNode >) {
		return JSON.stringify(this.documentToJson(nodes), null, 4);
	}

	private documentToJson(nodes: Array < TreeNode >) {
		let object = {};
		for (let node of nodes) {
			if (node.data.type == 'array') {
				object[node.data.key] = node.children.map(child => this.documentToJson(child.children));
			}
			else if (node.data.type == 'object') {
				object[node.data.key] = this.documentToJson(node.children);
			}
			else {
				object[node.data.key] = node.data.value;
			}
		}
		return object;
	}


	public set queryTerm(value: string) {
		this._queryTerm = value;
		this.search();
	}

	public get baseUrl() {
		return this.preferences.baseUrl;
	}

	public set baseUrl(value: string) {
		this.preferences.baseUrl = value;
		this.preferencesService.savePreferences(this.preferences);
		this.updateAvailableTypes();
	}


	public get embeddedMode(){
		const href = this.document.location.href;
		const suffix = "browse/#/";
		return href.endsWith(suffix);
	}


	private updateAvailableTypes() {
		if (_.isEmpty(this.baseUrl)) {
			this.availableTypes = [];
		}
		else {
			let url = this.baseUrl;
			if(!url.endsWith("/")){
				url = url + "/";
			}
			url = url + 'meta/resource?sort=resourceType&page[limit]=1000';

			this.http.get(url).subscribe(response => {
				let data = response.json()['data'] as Array<any>;
				this.availableTypes = [];
				this.availableTypes.push(...data.map(it => {
					return {label: it.attributes.resourceType, value: it.attributes.resourceType};
				}));
				this.availableTypes.push({
					label: null,
					value: null
				});

				let selectedType = this.preferences.query.type;
				if(selectedType && _.indexOf(data.map(it => it.attributes.resourceType), selectedType) == -1){
					this.preferences.query.type = null;
					this.preferencesService.savePreferences(this.preferences);
				}

				this.response = null;
			}, error => {
				this.availableTypes = [];
				this.response = error;
			});
		}
	}

	public updateAvailableRelationships() {
		if (_.isEmpty(this.preferences.query.type)) {
			this.availableRelationships = [];
		}
		else {
			let includes = 'attributes.type.elementType';
			let url = this.baseUrl;
			if(!url.endsWith("/")){
				url = url + "/";
			}
			url = url + 'meta/resource?include=' + includes + '&page[limit]=1000';
			this.http.get(url).subscribe(response => {
				let document = response.json();

				this.metaResources = {};
				for (let metaResource of document.data as Array<any>) {
					this.metaResources[metaResource.attributes.resourceType] = metaResource;
					this.service.metaElements[metaResource.id] = metaResource;
				}
				this.metaResource = this.metaResources[this.preferences.query.type];

				this.service.metaElements[this.metaResource.id] = this.metaResource;
				let included = document['included'] as Array<any>;
				for (let includedElement of included) {
					this.service.metaElements[includedElement.id] = includedElement;
				}
				this.metaAttributes = this.metaResource.relationships.attributes.data.map(it => this.service.resolveMeta(it));

				let relationships = this.metaAttributes.filter(it => it.attributes.association && it.type == 'meta/resourceField')
					.map(it => {
						return {label: it.attributes.name, value: it.attributes.name};
					});
				let nullValue = {label: '', value: null};
				this.availableRelationships = _.concat([nullValue], relationships);
			}, error => {
				this.metaResource = null;
				this.availableRelationships = [];
			});
		}
	}

	private isCollection(attr: any) {
		let type = attr.relationships.type.data.type;
		return type == 'meta/setType' || type == 'meta/listType' || type == 'meta/arrayType';
	}

	public setupPost() {

		let type = this.metaResource.attributes.resourceType;

		let attrs = _.join(
			this.metaAttributes.filter(it => !it.attributes.association).map(it => '"' + it.attributes.name + '": null'),
			',\n            ');
		let relationships = _.join(this.metaAttributes.filter(it => it.attributes.association)
				.map(it => '"' + it.attributes.name + '": {"data": ' + (this.isCollection(it) ? '[]' : 'null') + '}'),
			',\n            ');


		this.documentText =
			`{
    "data": {
        "id" : "<ID>",
        "type" : "${type}",
        "attributes": {
            ${attrs}
        },
        "relationships": {
            ${relationships}
        }
    }
}`;
		this.editable = true;
		this.clearDocumentNode();
		this.syncDocumentJsonText();
	}

	private clearDocumentNode() {
		this.documentNodes = null;
		this.documentError = null;
		this.filteredDocumentNodes = null;
	}

	public post() {
		this.syncDocumentJsonText();

		this.httpMethod = 'POST';
		this.preferences.query.id = null;

		let postQuery = {
			type: this.preferences.query.type
		};
		let url = this.getUrl(postQuery);
		this.http.post(url, this.documentText, this.getOptions()).subscribe(response => {
			this.handleResponse(response);
		}, response => {
			this.handleResponse(response);
		});
	}

	private getOptions() {
		let headers = new Headers({'Content-Type': 'application/vnd.api+json'});
		return new RequestOptions({headers: headers});
	}

	public patch() {
		this.syncDocumentJsonText();

		this.httpMethod = 'PATCH';

		let patchQuery = {
			type: this.preferences.query.type,
			id: this.preferences.query.id
		};
		let url = this.getUrl(patchQuery);
		this.http.patch(url, this.documentText, this.getOptions()).subscribe(response => {
			this.handleResponse(response);
		}, response => {
			this.handleResponse(response);
		});
	}

	public delete() {
		this.httpMethod = 'DELETE';

		let deleteQuery = {
			type: this.preferences.query.type,
			id: this.preferences.query.id
		};
		let url = this.getUrl(deleteQuery);
		this.http.delete(url, this.getOptions()).subscribe(response => {
			this.handleResponse(response);
		}, response => {
			this.handleResponse(response);
		});
	}


	public get(newQuery ?: any) {
		if (!_.isEmpty(this.baseUrl) && !_.isEmpty(this.preferences.query.type)) {
			this.httpMethod = 'GET';

			this.preferencesService.savePreferences(this.preferences);

			if (newQuery) {
				this.preferences.query = _.cloneDeep(newQuery);
			}
			let url = this.getUrl(this.preferences.query);
			this.http.get(url).subscribe(response => {
				this.handleResponse(response);
			}, response => {
				this.handleResponse(response);
			});
		}
		else {
			this.documentNodes = null;
			this.filteredDocumentNodes = null;
		}
	}

	private setDocument(text: string) {
		this.documentText = text;
		this.syncDocumentJsonText();
	}

	private handleResponse(response: Response) {

		this.response = response;
		if (response.status < 300 && !_.isEmpty(response.text())) {
			this.setDocument(response.text());
		}
		else {
			if (this.httpMethod == 'GET') {
				// all to otherwise continue editing
				this.documentText = null;
			}
			this.filteredDocumentNodes = null;
		}
		this.editable = !_.isEmpty(this.preferences.query.id) || this.httpMethod != 'GET';

		return false;
	}

	public getUrl(query: any): string {
		return _.join([this.utils.normalize(this.baseUrl, '/'), this.utils.normalize(query.type, '/'),
			this.utils.normalize(query.id, '/'), this.utils.normalize(query.relationship, '/'),
			"?" + query.parameters], '');
	}


	public search() {
		this.filteredDocumentNodes = [];
		for (let node of this.documentNodes) {
			let searchedNode = this.searchNode(node);
			if (searchedNode) {
				this.filteredDocumentNodes.push(searchedNode);
			}
		}
	}

	private matchesQueryTerm(value: string) {
		if (this.queryTerm.length == 0) {
			return true;
		}
		let queryElements = this.queryTerm.split(",").map(it => it.trim());
		for (let queryElement of queryElements) {
			if (value.indexOf(queryElement) != -1) {
				return true;
			}
		}
		return false;
	}

	private searchNode(node: TreeNode): TreeNode {
		let visibleChildren = [];
		if (node.children) {
			for (let child of node.children) {
				let searchedChild = this.searchNode(child);
				if (searchedChild) {
					visibleChildren.push(searchedChild);
				}
			}
		}


		let visible =
			visibleChildren.length > 0 || this.matchesQueryTerm(node.data.key) ||
			!_.isEmpty(node.data.value) && this.matchesQueryTerm(node.data.value);

		if (visible) {
			let clone = _.clone(node);
			clone.children = visibleChildren;
			return clone;
		}
		else {
			return null;
		}
	}

	public suggestParameters(event) {
		this.parameterSuggestions = this.service.suggestParameters(this.metaResource, event);
	}

	private toNodes(object: any): Array < TreeNode > {
		let nodes: Array < TreeNode > = [];
		for (let key in object) {
			if (key == 'resultIds' || key == 'loading' || key == 'query') {
				continue; // TODO
			}

			if (object.hasOwnProperty(key)) {
				let value = object[key];
				let data: any = {
					key: key
				};
				let children = [];
				if (_.isArray(value)) {
					data.type = 'array';
					for (let index in (value as Array<any>)) {
						let element = value[index];
						let child: TreeNode = {
							data: {
								key: index
							},
							expanded: true,
							children: this.toNodes(element)
						};
						children.push(child);
					}
				}
				else if (_.isObject(value)) {
					data.type = 'object';
					children = this.toNodes(value);
				}
				else {
					data.value = value;
					data.type = 'text';

					if (key == 'id' && object.type) {
						data.type = 'query';
						data.query = {
							type: object.type,
							id: object.id,
						};
					}
					else if (key == 'type' && object.id) {
						data.type = 'query';
						data.query = {
							type: object.type
						};
					}
					else if (!_.isEmpty(value) && _.isString(value) &&
						(value.startsWith("http://") || value.startsWith("https://"))) {

						let query: any = {
							type: null,
							id: "",
							relationship: "",
							parameters: ""
						};

						if (value.toString().startsWith(this.baseUrl)) {
							data.type = 'query';
							data.urlSuffix = value.substring(this.baseUrl.length);

							let path = value.substring(this.baseUrl.length);
							let paramSep = path.indexOf("?");

							query.parameters = paramSep == -1 ? '' : path.substring(paramSep + 1);
							path = paramSep == -1 ? path : path.substring(0, paramSep);
							path = this.utils.stripSeparators(path);

							for (let someType of this.availableTypes) {
								if (path.startsWith(someType)) {
									query.type = someType;
									path = path.substring(query.type.length + 1);
									break;
								}
							}

							if (query.type) {
								let pathElements = path.split("/");
								if (pathElements.length >= 1) {
									query.id = pathElements[0];
								}
								if (pathElements.length >= 2) {
									query.id = pathElements[0];
								}
								if (pathElements.length >= 3) {
									query.type = null; // cannot handle this
								}
							}
						}

						if (query.type) {
							data.type = 'query';
							data.query = query;
						}
						else {
							data.type = 'url';
							data.urlValue = value;
						}

					}
				}

				let node: TreeNode = {
					expanded: true,
					data: data,
					children: children
				};
				this.setExpandedState(node);
				nodes.push(node);
			}
		}

		return nodes;
	}

	private updatedExpandedStates(nodes ?) {
		let root = !nodes;
		if (root) {
			nodes = [...this.documentNodes];
		}

		for (let node of nodes) {
			this.setExpandedState(node);
			if (node.children) {
				this.updatedExpandedStates(node.children);
			}
		}

		if (root) {
			this.documentNodes = nodes;
			this.search();
		}
	}

	private setExpandedState(node) {
		if (node.data.key == 'links') {
			node.expanded = this.preferences.expand.links;
		}
		else if (node.data.key == 'meta') {
			node.expanded = this.preferences.expand.meta;
		}
		else if (node.data.key == 'attributes') {
			node.expanded = this.preferences.expand.attributes;
		}
		else if (node.data.key == 'relationships') {
			node.expanded = this.preferences.expand.relationships;
		}
	}

}
