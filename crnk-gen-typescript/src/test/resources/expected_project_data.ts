import {
	ArrayPath,
	BeanPath,
	MapPath,
	StringPath
} from '@crnk/angular-ngrx';

export interface ProjectData {
	data?: string;
	keywords?: Array<string>;
	customData?: { [key: string]: string };
	dueDate?: string;
}
export class QProjectData extends BeanPath<ProjectData> {
	metaId = 'resources.types.projectdata';
	data: StringPath = this.createString('data');
	keywords: ArrayPath<StringPath> = new ArrayType(this, 'keywords', StringPath);
	customData: MapPath<StringPath> = new MapType(this, 'customData', StringPath);
	dueDate: StringPath = this.createString('dueDate');
}