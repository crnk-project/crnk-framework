export interface OneResourceRelationship<T extends Resource> {
	data?: T;
	meta?: any;
	links?: any;
}

export interface ManyResourceRelationship<T extends Resource> {
	data?: Array<T>;
	meta?: any;
	links?: any;
}

export interface ResourceErrorSource {
	pointer?: string;
	parameter?: string;
}

export interface ResourceError {
	id?: string;
	links?: any;
	status?: string;
	code?: string;
	title?: string;
	detail?: string;
	source?: ResourceErrorSource;
	meta?: any;
}

export interface Resource {
	type?: string;
	id?: string;
	meta?: any;
	links?: any;
	errors?: Array<ResourceError>;
}

export interface Result {

	data?: Resource | Array<Resource>;

	meta?: any;

	links?: any;

	errors: Array<ResourceError>;
}

export interface ManyResult extends Result {
	data?: Array<Resource>;
}

export interface OneResult extends Result {
	data?: Resource;
}