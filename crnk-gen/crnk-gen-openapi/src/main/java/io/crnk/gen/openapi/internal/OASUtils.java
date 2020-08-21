package io.crnk.gen.openapi.internal;

import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.gen.openapi.internal.schemas.AbstractSchemaGenerator;
import io.crnk.gen.openapi.internal.schemas.ResourceReference;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaCollectionType;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaLiteral;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.Validate.notNull;

public class OASUtils {

	public static Stream<MetaResourceField> attributes(MetaResource metaResource, boolean includePrimaryKey) {
		return metaResource.getChildren().stream()
				.filter(not(MetaPrimaryKey.class::isInstance))
				.map(MetaResourceField.class::cast)
				.filter(it -> it.getFieldType() != ResourceFieldType.META_INFORMATION && it.getFieldType() != ResourceFieldType.LINKS_INFORMATION)
				.filter(e -> !e.isPrimaryKeyAttribute() || includePrimaryKey);
	}

	public static Stream<MetaResourceField> notAssociationAttributes(Stream<MetaResourceField> attributes) {
		return attributes.filter(not(MetaResourceField::isAssociation));
	}

	public static Stream<MetaResourceField> associationAttributes(Stream<MetaResourceField> attributes) {
		return attributes.filter(MetaAttribute::isAssociation);
	}

	public static Stream<MetaResourceField> notAssociationAttributes(MetaResource metaResource, boolean includePrimaryKey) {
		return notAssociationAttributes(attributes(metaResource, includePrimaryKey));
	}

	public static Stream<MetaResourceField> associationAttributes(MetaResource metaResource, boolean includePrimaryKey) {
		return associationAttributes(attributes(metaResource, includePrimaryKey));
	}

	public static Stream<MetaResourceField> filterAttributes(MetaResource metaResource, boolean includePrimaryKey) {
		return attributes(metaResource, includePrimaryKey).filter(MetaAttribute::isFilterable);
	}

	public static Stream<MetaResourceField> patchAttributes(MetaResource metaResource, boolean includePrimaryKey) {
		return attributes(metaResource, includePrimaryKey).filter(MetaAttribute::isUpdatable);
	}

	public static Stream<MetaResourceField> postAttributes(MetaResource metaResource, boolean includePrimaryKey) {
		return attributes(metaResource, includePrimaryKey).filter(MetaAttribute::isInsertable);
	}

	public static Stream<MetaResourceField> sortAttributes(MetaResource metaResource, boolean includePrimaryKey) {
		return attributes(metaResource, includePrimaryKey).filter(MetaAttribute::isSortable);
	}

	public static MetaResourceField getPrimaryKeyMetaResourceField(MetaResource metaResource) {
		return metaResource.getChildren().stream()
				.filter(MetaResourceField.class::isInstance)
				.filter(e -> ((MetaResourceField) e).isPrimaryKeyAttribute())
				.map(MetaResourceField.class::cast)
				.findFirst().get();
	}

	public static <T> Predicate<T> not(Predicate<T> p) {
		return o -> !p.test(o);
	}

	public static Schema transformMetaResourceField(MetaType metaType) {
		if (metaType.getImplementationClass() == byte[].class) {
			return new StringSchema();
		}
		if (metaType instanceof MetaResource) {
			return new ResourceReference((MetaResource) metaType).$ref();
		} else if (metaType instanceof MetaCollectionType) {
			return new ArraySchema()
					.items(transformMetaResourceField(metaType.getElementType()))
					.uniqueItems(metaType instanceof MetaSetType);
		} else if (metaType instanceof MetaArrayType) {
			return new ArraySchema()
					.items(transformMetaResourceField(metaType.getElementType()))
					.uniqueItems(false);
		} else if (metaType instanceof MetaJsonObject) {
			ObjectSchema objectSchema = new ObjectSchema();
			for (MetaElement child : metaType.getChildren()) {
				if (child instanceof MetaAttribute) {
					MetaAttribute metaAttribute = (MetaAttribute) child;
					objectSchema.addProperties(child.getName(), transformMetaResourceField(metaAttribute.getType()));
				}
			}
			return objectSchema;
		} else if (metaType.getName().equals("boolean")) {
			return new BooleanSchema();
		} else if (metaType.getName().equals("byte")) {
			return new ByteArraySchema();
		} else if (metaType.getName().equals("date")) {
			return new DateSchema();
		} else if (metaType.getName().equals("offsetDateTime")) {
			return new DateTimeSchema();
		} else if (metaType.getName().equals("localDate")) {
			return new DateSchema();
		} else if (metaType.getName().equals("localDateTime")) {
			return new DateTimeSchema();
		} else if (metaType.getName().equals("double")) {
			return new NumberSchema().format("double");
		} else if (metaType.getName().equals("float")) {
			return new NumberSchema().format("float");
		} else if (metaType.getName().equals("integer")) {
			return new IntegerSchema().format("int32");
		} else if (metaType.getName().equals("json")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("json.object")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("json.array")) {
			return new ArraySchema().items(new Schema());
		} else if (metaType.getName().equals("long")) {
			return new IntegerSchema().format("int64");
		} else if (metaType.getName().equals("object")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("short")) {
			return new IntegerSchema().minimum(BigDecimal.valueOf(Short.MIN_VALUE)).maximum(BigDecimal.valueOf(Short.MAX_VALUE));
		} else if (metaType.getName().equals("string")) {
			return new StringSchema();
		} else if (metaType.getName().equals("uuid")) {
			return new UUIDSchema();
		} else if (metaType instanceof MetaMapType) {
			return new ObjectSchema().additionalProperties(transformMetaResourceField(metaType.getElementType()));
		} else if (metaType instanceof MetaEnumType) {
			Schema enumSchema = new StringSchema();
			for (MetaElement child : metaType.getChildren()) {
				if (child instanceof MetaLiteral) {
					enumSchema.addEnumItemObject(child.getName());
				} else {
					return new ObjectSchema().additionalProperties(true);
				}
			}
			return enumSchema;
		} else {
			return new Schema().type(metaType.getElementType().getName());
		}
	}

	public static boolean oneToMany(MetaResourceField metaResourceField) {
		return metaResourceField.getType().isCollection() || metaResourceField.getType().isMap();
	}

	public static String getResourcesPath(MetaResource metaResource) {
		//
		// TODO: Requires access to CrnkBoot.getWebPathPrefix() and anything that might modify a path
		// TODO: alternatively, have a config setting for this generator that essentially duplicates the above
		//
		return "/" + metaResource.getResourcePath();
	}

	public static String getResourcePath(MetaResource metaResource) {
		StringBuilder keyPath = new StringBuilder(getResourcesPath(metaResource) + "/");
		for (MetaAttribute metaAttribute : metaResource.getPrimaryKey().getElements()) {
			keyPath.append("{");
			keyPath.append(metaAttribute.getName());
			keyPath.append("}");
		}
		return keyPath.toString();
	}

	public static String getNestedPath(MetaResource metaResource, MetaResourceField metaResourceField) {
		return getResourcePath(metaResource) + "/" + metaResourceField.getName();
	}

	public static String getRelationshipsPath(MetaResource metaResource, MetaResourceField metaResourceField) {
		return getResourcePath(metaResource) + "/relationships/" + metaResourceField.getName();
	}

	public static List<Schema> getIncludedSchemaRefs(AbstractSchemaGenerator... schemas) {
		notNull(schemas, "schemas cannot be null");
		return Stream.of(schemas)
				.filter(AbstractSchemaGenerator::isIncluded)
				.map(AbstractSchemaGenerator::$ref)
				.collect(Collectors.toList());
	}
}
