import {
	ManyResult,
	OneResult,
	Resource
} from './crnk';
import {ScheduleStatus} from './schedule.status';

export interface PrimitiveAttribute extends Resource {
	stringValue?: string;
	intValue?: number;
	longValue?: number;
	booleanValue?: boolean;
	floatValue?: number;
	shortValue?: number;
	doubleValue?: number;
	jsonNodeValue?: any;
	arrayNodeValue?: any;
	objectNodeValue?: any;
	nullableLongValue?: number;
	nullableBooleanValue?: boolean;
	nullableByteValue?: number;
	nullableShortValue?: number;
	nullableIntegerValue?: number;
	nullableFloatValue?: number;
	nullableDoubleValue?: number;
	uuidValue?: string;
	dateValue?: any;
	objectValue?: any;
	mapValueWithEnumKey?: { [key: ScheduleStatus]: string };
	mapValueWithListValue?: { [key: string]: Array<string> };
	mapValueWithSetValue?: { [key: string]: Array<string> };
}
export interface PrimitiveAttributeResult extends OneResult {
	data?: PrimitiveAttribute;
}
export interface PrimitiveAttributeListResult extends ManyResult {
	data?: Array<PrimitiveAttribute>;
}
export let createEmptyPrimitiveAttribute = function(id: string): PrimitiveAttribute {
	return {
		id: id,
		type: 'primitiveAttribute',
	};
};