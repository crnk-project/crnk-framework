package io.crnk.core.engine.information.resource;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.parser.StringMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.MultipleJsonApiLinksInformationException;
import io.crnk.core.exception.MultipleJsonApiMetaInformationException;
import io.crnk.core.exception.ResourceDuplicateIdException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.resource.annotations.JsonApiResource;

/**
 * Holds information about the type of the resource.
 */
public class ResourceInformation {

	private final Class<?> resourceClass;

	/**
	 * Found field of the id. Each resource has to contain a field marked by
	 * JsonApiId annotation.
	 */
	private ResourceField idField;

	/**
	 * A set of resource's attribute fields.
	 */
	private List<ResourceField> attributeFields;

	/**
	 * A set of fields that contains non-standard Java types (List, Set, custom
	 * classes, ...).
	 */
	private List<ResourceField> relationshipFields;

	/**
	 * An underlying field's name which contains meta information about for a
	 * resource
	 */
	private ResourceField metaField;

	/**
	 * An underlying field's name which contain links information about for a
	 * resource
	 */
	private ResourceField linksField;

	/**
	 * Type name of the resource. Corresponds to {@link JsonApiResource#type}
	 * for annotated resources.
	 */
	private final String resourceType;

	/**
	 * Type url path of the resource. Corresponds to {@link JsonApiResource#resourcePath}
	 * for annotated resources.
	 */
	private final String resourcePath;

	/**
	 * Creates a new instance of the given resource.
	 */
	private ResourceInstanceBuilder<?> instanceBuilder;

	private final TypeParser parser;

	/**
	 * Resource type of the super type.
	 */
	private String superResourceType;

	private Map<String, ResourceField> fieldByJsonName = new HashMap<>();

	private Map<String, ResourceField> fieldByUnderlyingName = new HashMap<>();

	private List<ResourceField> fields;

	private AnyResourceFieldAccessor anyFieldAccessor;

	private ResourceValidator validator;

	private Class<? extends PagingSpec> pagingSpecType;

	private StringMapper idStringMapper = new StringMapper() {
		@Override
		public String toString(Object input) {
			return input.toString();
		}

		@Override
		public Object parse(String input) {
			Class idType = getIdField().getType();
			return parser.parse(input, idType);
		}
	};

	public ResourceInformation(TypeParser parser, Class<?> resourceClass, String resourceType, String superResourceType,
			List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
		this(parser, resourceClass, resourceType, null, superResourceType, null, fields, pagingSpecType);
	}

	public ResourceInformation(TypeParser parser, Class<?> resourceClass, String resourceType, String resourcePath,
			String superResourceType,
			List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
		this(parser, resourceClass, resourceType, resourcePath, superResourceType, null, fields, pagingSpecType);
	}

	public ResourceInformation(TypeParser parser, Class<?> resourceClass, String resourceType, String superResourceType,
			ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
		this(parser, resourceClass, resourceType, null, superResourceType, instanceBuilder, fields, pagingSpecType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResourceInformation(TypeParser parser, Class<?> resourceClass, String resourceType, String resourcePath,
			String superResourceType,
			ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
		this.parser = parser;
		this.resourceClass = resourceClass;
		this.resourceType = resourceType;
		this.resourcePath = resourcePath;
		this.superResourceType = superResourceType;
		this.instanceBuilder = instanceBuilder;
		this.fields = fields;
		this.pagingSpecType = pagingSpecType;

		initFields();
		if (this.instanceBuilder == null) {
			this.instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);
		}

		initAny();
	}

	@Deprecated
	public void setValidator(ResourceValidator validator) {
		this.validator = validator;
	}

	@Deprecated
	public ResourceValidator getValidator() {
		return validator;
	}

	@Deprecated
	public void setIdStringMapper(StringMapper idStringMapper) {
		this.idStringMapper = idStringMapper;
	}

	public StringMapper getIdStringMapper() {
		return idStringMapper;
	}

	public AnyResourceFieldAccessor getAnyFieldAccessor() {
		return anyFieldAccessor;
	}

	private void initAny() {
		final Method jsonAnyGetter = ClassUtils.findMethodWith(resourceClass, JsonAnyGetter.class);
		final Method jsonAnySetter = ClassUtils.findMethodWith(resourceClass, JsonAnySetter.class);

		if (absentAnySetter(jsonAnyGetter, jsonAnySetter)) {
			throw new InvalidResourceException(
					String.format("A resource %s has to have both methods annotated with @JsonAnySetter and @JsonAnyGetter",
							resourceClass.getCanonicalName()));
		}

		if (jsonAnyGetter != null) {
			anyFieldAccessor = new AnyResourceFieldAccessor() {

				@Override
				public Object getValue(Object resource, String name) {
					try {
						return jsonAnyGetter.invoke(resource, name);
					}
					catch (IllegalAccessException | InvocationTargetException e) {
						throw new ResourceException(
								String.format("Exception while reading %s.%s due to %s", resource, name, e.getMessage()), e);
					}
				}

				@Override
				public void setValue(Object resource, String name, Object fieldValue) {
					try {
						jsonAnySetter.invoke(resource, name, fieldValue);
					}
					catch (IllegalAccessException | InvocationTargetException e) {
						throw new ResourceException(
								String.format("Exception while writting %s.%s=%s due to %s", resource, name, fieldValue,
										e.getMessage()), e);
					}
				}
			};
		}
	}

	/**
	 * The resource has to have both method annotated with {@link JsonAnySetter} and {@link JsonAnyGetter} to allow
	 * proper handling.
	 *
	 * @return <i>true</i> if resource definition is incomplete, <i>false</i> otherwise
	 */
	private static boolean absentAnySetter(Method jsonAnyGetter, Method jsonAnySetter) {
		return (jsonAnySetter == null && jsonAnyGetter != null) ||
				(jsonAnySetter != null && jsonAnyGetter == null);
	}

	private void initFields() {
		if (fields != null) {
			List<ResourceField> idFields = ResourceFieldType.ID.filter(fields);
			if (idFields.size() > 1) {
				throw new ResourceDuplicateIdException(resourceClass.getCanonicalName());
			}

			this.idField = idFields.isEmpty() ? null : idFields.get(0);

			this.attributeFields = ResourceFieldType.ATTRIBUTE.filter(fields);
			this.relationshipFields = ResourceFieldType.RELATIONSHIP.filter(fields);

			this.metaField = getMetaField(resourceClass, fields);
			this.linksField = getLinksField(resourceClass, fields);

			for (ResourceField resourceField : fields) {
				resourceField.setResourceInformation(this);
				fieldByJsonName.put(resourceField.getJsonName(), resourceField);
				fieldByUnderlyingName.put(resourceField.getUnderlyingName(), resourceField);
			}
		}
		else {
			this.relationshipFields = Collections.emptyList();
			this.attributeFields = Collections.emptyList();
			this.metaField = null;
			this.linksField = null;
			this.idField = null;
		}
	}

	@Deprecated
	public void setFields(List<ResourceField> fields) {
		this.fields = fields;
		this.initFields();
	}

	private static <T> ResourceField getMetaField(Class<T> resourceClass, Collection<ResourceField> classFields) {
		List<ResourceField> metaFields = new ArrayList<>(1);
		for (ResourceField field : classFields) {
			if (field.getResourceFieldType() == ResourceFieldType.META_INFORMATION) {
				metaFields.add(field);
			}
		}

		if (metaFields.isEmpty()) {
			return null;
		}
		else if (metaFields.size() > 1) {
			throw new MultipleJsonApiMetaInformationException(resourceClass.getCanonicalName());
		}
		return metaFields.get(0);
	}

	private static <T> ResourceField getLinksField(Class<T> resourceClass, Collection<ResourceField> classFields) {
		List<ResourceField> linksFields = new ArrayList<>(1);
		for (ResourceField field : classFields) {
			if (field.getResourceFieldType() == ResourceFieldType.LINKS_INFORMATION) {
				linksFields.add(field);
			}
		}

		if (linksFields.isEmpty()) {
			return null;
		}
		else if (linksFields.size() > 1) {
			throw new MultipleJsonApiLinksInformationException(resourceClass.getCanonicalName());
		}
		return linksFields.get(0);
	}

	public String getResourceType() {
		return resourceType;
	}

	public String getResourcePath() {
		if (resourcePath == null) {
			return resourceType;
		}
		return resourcePath;
	}

	public String getSuperResourceType() {
		return superResourceType;
	}

	public <T> ResourceInstanceBuilder<T> getInstanceBuilder() {
		return (ResourceInstanceBuilder<T>) instanceBuilder;
	}

	public Class<?> getResourceClass() {
		return resourceClass;
	}

	public ResourceField getIdField() {
		return idField;
	}

	public List<ResourceField> getAttributeFields() {
		return attributeFields;
	}

	public List<ResourceField> getRelationshipFields() {
		return relationshipFields;
	}

	public ResourceField findFieldByName(String name) {
		return fieldByJsonName.get(name);
	}

	public ResourceField findFieldByUnderlyingName(String name) {
		return fieldByUnderlyingName.get(name);
	}

	public ResourceField findRelationshipFieldByName(String name) {
		ResourceField resourceField = fieldByJsonName.get(name);
		return resourceField != null && resourceField.getResourceFieldType() == ResourceFieldType.RELATIONSHIP ? resourceField
				: null;
	}

	public ResourceField findAttributeFieldByName(String name) {
		ResourceField resourceField = fieldByJsonName.get(name);
		return resourceField != null && resourceField.getResourceFieldType() == ResourceFieldType.ATTRIBUTE ? resourceField
				: null;
	}

	public ResourceField getMetaField() {
		return metaField;
	}

	public ResourceField getLinksField() {
		return linksField;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResourceInformation that = (ResourceInformation) o;
		return Objects.equals(resourceClass, that.resourceClass) && Objects.equals(resourceType, that.resourceType) && Objects
				.equals(resourcePath, that.resourcePath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceClass, resourceType, resourcePath);
	}

	/**
	 * Converts the given id to a string.
	 *
	 * @param id id
	 * @return stringified id
	 */
	public String toIdString(Object id) {
		if (id == null) {
			return null;
		}
		return idStringMapper.toString(id);
	}

	/**
	 * @param resourceOrId resource or id object
	 * @return ResourceIdentifier of that resource
	 */
	public ResourceIdentifier toResourceIdentifier(Object resourceOrId) {
		if (resourceOrId == null) {
			return null;
		}
		if (resourceOrId instanceof Resource) {
			return ((Resource) resourceOrId).toIdentifier();
		}
		if (resourceClass.isInstance(resourceOrId)) {
			resourceOrId = getId(resourceOrId);
		}
		if (resourceOrId instanceof ResourceIdentifier) {
			return (ResourceIdentifier) resourceOrId;
		}
		String strId;
		if (resourceOrId instanceof String) {
			strId = (String) resourceOrId;
		}
		else {
			strId = toIdString(resourceOrId);
		}
		return new ResourceIdentifier(strId, getResourceType());

	}

	/**
	 * Converts the given id string into its object representation.
	 *
	 * @param id stringified id
	 * @return id
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Serializable parseIdString(String id) {
		return (Serializable) idStringMapper.parse(id);
	}

	/**
	 * @return id of the resource
	 */
	public Object getId(Object resource) {
		return idField.getAccessor().getValue(resource);
	}

	public void setId(Object resource, Object id) {
		idField.getAccessor().setValue(resource, id);
	}

	public void verify(Object resource, Document requestDocument) {
		// nothing to do
		if (validator != null) {
			validator.validate(resource, requestDocument);
		}
	}

	public List<ResourceField> getFields() {
		return Collections.unmodifiableList(fields);
	}

	public Class<? extends PagingSpec> getPagingSpecType() {
		return pagingSpecType;
	}
}