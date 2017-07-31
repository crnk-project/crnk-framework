package io.crnk.jpa.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder.AnnotatedResourceField;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.utils.Optional;
import io.crnk.jpa.annotations.JpaResource;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.information.MetaAwareInformation;
import io.crnk.meta.model.*;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Extracts resource information from JPA and Crnk annotations. Crnk
 * annotations take precedence.
 */
public class JpaResourceInformationBuilder implements ResourceInformationBuilder {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private MetaLookup jpaMetaLookup;

	private ResourceInformationBuilderContext context;

	public JpaResourceInformationBuilder(MetaLookup jpaMetaLookup) {
		this.jpaMetaLookup = jpaMetaLookup;
	}

	public static boolean isJpaLazy(Collection<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof ElementCollection) {
				return ((ElementCollection) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof ManyToOne) {
				return ((ManyToOne) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof OneToMany) {
				return ((OneToMany) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof ManyToMany) {
				return ((ManyToMany) annotation).fetch() == FetchType.LAZY;
			}
		}
		return false;
	}

	public static String getJpaOppositeName(Collection<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof OneToMany) {
				return StringUtils.emptyToNull(((OneToMany) annotation).mappedBy());
			} else if (annotation instanceof ManyToMany) {
				return StringUtils.emptyToNull(((ManyToMany) annotation).mappedBy());
			}
		}
		return null;
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		// needs to be configured for being exposed
		if (resourceClass.getAnnotation(JpaResource.class) != null) {
			return true;
		}

		// needs to be an entity
		MetaElement meta = jpaMetaLookup.getMeta(resourceClass, MetaJpaDataObject.class, true);
		if (meta instanceof MetaEntity) {
			MetaEntity metaEntity = (MetaEntity) meta;
			MetaKey primaryKey = metaEntity.getPrimaryKey();
			return primaryKey != null && primaryKey.getElements().size() == 1;
		} else {
			// note that DTOs cannot be handled here
			return meta instanceof MetaJpaDataObject;
		}
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public ResourceInformation build(final Class<?> resourceClass) {
		String resourceType = getResourceType(resourceClass);

		MetaDataObject meta;
		DefaultResourceInstanceBuilder instanceBuilder;

		meta = jpaMetaLookup.getMeta(resourceClass, MetaJpaDataObject.class).asDataObject();
		instanceBuilder = new JpaResourceInstanceBuilder((MetaJpaDataObject) meta, resourceClass);

		List<ResourceField> fields = buildFields(meta);
		Set<String> ignoredFields = getIgnoredFields(meta);

		Class<?> superclass = resourceClass.getSuperclass();
		String superResourceType = superclass != Object.class
				&& superclass.getAnnotation(MappedSuperclass.class) == null ? context.getResourceType(superclass)
				: null;

		TypeParser typeParser = context.getTypeParser();
		return new JpaResourceInformation(typeParser, meta, resourceClass, resourceType, superResourceType,
				instanceBuilder, fields, ignoredFields);
	}

	@Override
	public String getResourceType(Class<?> entityClass) {
		JpaResource annotation = entityClass.getAnnotation(JpaResource.class);
		if (annotation != null) {
			return annotation.type();
		}
		if (entityClass.getAnnotation(MappedSuperclass.class) != null) {
			return null; // super classes do not have a document type
		}

		String name = entityClass.getSimpleName();
		if (name.endsWith(ENTITY_NAME_SUFFIX)) {
			name = name.substring(0, name.length() - ENTITY_NAME_SUFFIX.length());
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	protected List<ResourceField> buildFields(MetaDataObject meta) {
		List<ResourceField> fields = new ArrayList<>();

		for (MetaAttribute attr : meta.getAttributes()) {
			if (!isIgnored(attr)) {
				fields.add(toField(meta, attr));
			}
		}

		return fields;
	}

	protected boolean isAssociation(MetaDataObject meta, MetaAttribute attr) {
		return attr.isAssociation();
	}

	protected Set<String> getIgnoredFields(MetaDataObject meta) {
		Set<String> fields = new HashSet<>();
		for (MetaAttribute attr : meta.getAttributes()) {
			if (isIgnored(attr)) {
				fields.add(attr.getName());
			}
		}
		return fields;
	}

	protected boolean isIgnored(MetaAttribute attr) {
		return attr.getAnnotation(JsonIgnore.class) != null;
	}

	protected ResourceField toField(MetaDataObject meta, MetaAttribute attr) {
		String jsonName = attr.getName();
		String underlyingName = attr.getName();
		Class<?> type = attr.getType().getImplementationClass();
		Type genericType = attr.getType().getImplementationType();

		// meta model does not differentiate between long and Long, int and Integer etc. correct this here
		Method getter = ClassUtils.findGetter(meta.getImplementationClass(), underlyingName);
		Field field = ClassUtils.findClassField(meta.getImplementationClass(), underlyingName);
		if (getter != null) {
			type = getter.getReturnType();
			genericType = getter.getGenericReturnType();
		} else if (field != null) {
			type = field.getType();
			genericType = field.getGenericType();
		}

		Collection<Annotation> annotations = attr.getAnnotations();

		// use JPA annotations as default
		String oppositeName = getJpaOppositeName(annotations);
		boolean lazyDefault = isJpaLazy(annotations);

		// read Crnk annotations
		boolean lazy = AnnotatedResourceField.isLazy(annotations, lazyDefault);
		boolean includeByDefault = AnnotatedResourceField.getIncludeByDefault(annotations);

		MetaKey primaryKey = meta.getPrimaryKey();
		boolean id = primaryKey != null && primaryKey.getElements().contains(attr);
		boolean linksInfo = attr.getAnnotation(JsonApiLinksInformation.class) != null;
		boolean metaInfo = attr.getAnnotation(JsonApiMetaInformation.class) != null;
		boolean association = isAssociation(meta, attr);
		ResourceFieldType resourceFieldType = ResourceFieldType.get(id, linksInfo, metaInfo, association);
		String oppositeResourceType =
				association ? AnnotationResourceInformationBuilder.getResourceType(genericType, context) : null;

		boolean hasSetter = AnnotationResourceInformationBuilder.hasSetter(meta.getImplementationClass(), attr.getName());

		// versions must be sent along with request to implement proper optimistic locking
		boolean postable = attr.isInsertable() || attr.isVersion();
		boolean patchable = attr.isUpdatable() || attr.isVersion();
		ResourceFieldAccess access = new ResourceFieldAccess(postable, patchable, attr.isSortable(), attr.isFilterable());
		if (attr.getAnnotation(JsonApiField.class) != null) {
			// override JPA behavior with JSON API annotations
			access = AnnotationResourceInformationBuilder.getResourceFieldAccess(resourceFieldType, hasSetter, annotations);
		}

		// related repositories should lookup, we ignore the hibernate proxies
		LookupIncludeBehavior lookupIncludeBehavior = AnnotatedResourceField.getLookupIncludeBehavior(annotations,
				LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		return new JpaResourceField(attr, jsonName, underlyingName, resourceFieldType, type, genericType,
				oppositeResourceType, oppositeName, lazy, includeByDefault, lookupIncludeBehavior, access);
	}

	@Override
	public void init(ResourceInformationBuilderContext context) {
		this.context = context;
	}

	class JpaResourceInstanceBuilder<T> extends DefaultResourceInstanceBuilder<T> {

		private MetaJpaDataObject meta;

		public JpaResourceInstanceBuilder(MetaJpaDataObject meta, Class<T> resourceClass) {
			super(resourceClass);
			this.meta = meta;
		}

		@Override
		public int hashCode() {
			return super.hashCode() | meta.getName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) && obj instanceof JpaResourceInstanceBuilder;
		}
	}

	class JpaResourceInformation extends ResourceInformation implements MetaAwareInformation<MetaDataObject> {

		private MetaDataObject jpaMeta;

		private Set<String> ignoredFields;

		public JpaResourceInformation(TypeParser typeParser, MetaDataObject meta, Class<?> resourceClass,
									  String resourceType, String superResourceType, // NOSONAR
									  ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Set<String> ignoredFields) {
			super(typeParser, resourceClass, resourceType, superResourceType, instanceBuilder, fields);
			this.jpaMeta = meta;
			this.ignoredFields = ignoredFields;
		}

		@Override
		public void verify(Object entity, Document requestDocument) {
			// TODO consider implementing proper versioning/locking/timestamping mechanism
			checkOptimisticLocking(entity, requestDocument.getSingleData().get());
		}

		private void checkOptimisticLocking(Object entity, Resource resource) {
			MetaAttribute versionAttr = jpaMeta.getVersionAttribute();
			if (versionAttr != null) {
				JsonNode versionNode = resource.getAttributes().get(versionAttr.getName());
				if (versionNode != null) {
					Object requestVersion = context.getTypeParser().parse(versionNode.asText(),
							(Class) versionAttr.getType().getImplementationClass());
					Object currentVersion = versionAttr.getValue(entity);
					if (!currentVersion.equals(requestVersion)) {
						throw new OptimisticLockException(
								resource.getId() + " changed from version " + requestVersion + " to " + currentVersion);
					}
				}
			}
		}

		/**
		 * Specialized ID handling to take care of embeddables and compound
		 * primary keys.
		 */
		@Override
		public Serializable parseIdString(String id) {
			return fromKeyString(id);
		}

		private Serializable fromKeyString(String id) {

			MetaKey primaryKey = jpaMeta.getPrimaryKey();
			MetaAttribute attr = primaryKey.getUniqueElement();
			return (Serializable) fromKeyString(attr.getType(), id);
		}

		private Object fromKeyString(MetaType type, String idString) {
			// => support compound keys with unique ids
			if (type instanceof MetaDataObject) {
				return parseEmbeddableString((MetaDataObject) type, idString);
			} else {
				return context.getTypeParser().parse(idString, (Class) type.getImplementationClass());
			}
		}

		private Object parseEmbeddableString(MetaDataObject embType, String idString) {
			String[] keyElements = idString.split(MetaKey.ID_ELEMENT_SEPARATOR);

			Object id = ClassUtils.newInstance(embType.getImplementationClass());

			List<? extends MetaAttribute> embAttrs = embType.getAttributes();
			if (keyElements.length != embAttrs.size()) {
				throw new UnsupportedOperationException("failed to parse " + idString + " for " + embType.getId());
			}
			for (int i = 0; i < keyElements.length; i++) {
				MetaAttribute embAttr = embAttrs.get(i);
				Object idElement = fromKeyString(embAttr.getType(), keyElements[i]);
				embAttr.setValue(id, idElement);
			}
			return id;
		}

		/**
		 * Specialized ID handling to take care of embeddables and compound
		 * primary keys.
		 */
		@Override
		public String toIdString(Object id) {
			return jpaMeta.getPrimaryKey().toKeyString(id);
		}

		@Override
		public int hashCode() {
			return super.hashCode() | ignoredFields.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) && obj instanceof JpaResourceInformation;
		}

		@Override
		public Optional<MetaDataObject> getMetaElement() {
			return Optional.empty();
		}

		@Override
		public Optional<MetaDataObject> getProjectedMetaElement() {
			return Optional.of(jpaMeta);
		}
	}
}
