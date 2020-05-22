package io.crnk.data.jpa.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EmbeddedId;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OptimisticLockException;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.information.resource.ResourceValidator;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.information.resource.ResourceInformationProviderBase;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.parser.StringMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.utils.Prioritizable;
import io.crnk.data.jpa.meta.JpaMetaProvider;
import io.crnk.data.jpa.meta.MetaEntity;
import io.crnk.data.jpa.meta.MetaJpaDataObject;
import io.crnk.data.jpa.meta.MetaMappedSuperclass;
import io.crnk.data.jpa.meta.internal.JpaMetaUtils;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaType;

/**
 * Extracts resource information from JPA and Crnk annotations. Crnk
 * annotations take precedence.
 */
public class JpaResourceInformationProvider extends ResourceInformationProviderBase implements Prioritizable {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private final JpaMetaProvider metaProvider;

	public JpaResourceInformationProvider() {
		this(new NullPropertiesProvider());
	}

	public JpaResourceInformationProvider(PropertiesProvider propertiesProvider) {
		super(
				propertiesProvider,
				Arrays.asList(new DefaultResourceFieldInformationProvider(), new JpaResourceFieldInformationProvider(),
						new JacksonResourceFieldInformationProvider()));

		metaProvider = new JpaMetaProvider(Collections.emptySet());
		MetaLookupImpl lookup = new MetaLookupImpl();
		lookup.addProvider(metaProvider);
		lookup.initialize();
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		if (JpaMetaUtils.isJpaType(resourceClass)) {
			// needs to be an entity
			MetaElement meta = metaProvider.discoverMeta(resourceClass);
			if (meta instanceof MetaEntity || meta instanceof MetaMappedSuperclass && resourceClass.getAnnotation(JsonApiResource.class) != null) {
				MetaJpaDataObject metaEntity = (MetaJpaDataObject) meta;
				MetaKey primaryKey = metaEntity.getPrimaryKey();
				return primaryKey != null && primaryKey.getElements().size() == 1;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResourceInformation build(final Class<?> resourceClass) {
		String resourceType = getResourceType(resourceClass);
		String resourcePath = getResourcePath(resourceClass);

		MetaDataObject meta = metaProvider.discoverMeta(resourceClass).asDataObject();
		DefaultResourceInstanceBuilder instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);

		BeanInformation beanInformation = BeanInformation.get(resourceClass);

		ResourceFieldAccess resourceAccess = getResourceAccess(resourceClass);

		List<ResourceField> fields = getResourceFields(resourceClass, resourceAccess, false);
		handleIdOverride(resourceClass, fields);

		Class<?> superclass = resourceClass.getSuperclass();
		String superResourceType = superclass != Object.class
				&& superclass.getAnnotation(MappedSuperclass.class) == null ? context.getResourceType(superclass)
				: null;

		TypeParser typeParser = context.getTypeParser();
		ResourceInformation information =
				new ResourceInformation(typeParser, resourceClass, resourceType, resourcePath, superResourceType,
						instanceBuilder, fields, OffsetLimitPagingSpec.class);
		information.setValidator(new JpaOptimisticLockingValidator(meta));
		information.setAccess(resourceAccess);
		information.setVersionRange(getVersionRange(resourceClass));

		ResourceField idField = information.getIdField();
		BeanAttributeInformation idAttr = beanInformation.getAttribute(idField.getUnderlyingName());
		if (idAttr.getAnnotation(EmbeddedId.class).isPresent()) {
			information.setIdStringMapper(new JpaIdMapper(meta));
		}

		return information;
	}

	/**
	 * make sure that @JsonApiId wins over @Id and @EmbeddedId of JPA.
	 */
	private void handleIdOverride(Class<?> resourceClass, List<ResourceField> fields) {
		List<ResourceField> idFields = fields.stream()
				.filter(field -> field.getResourceFieldType() == ResourceFieldType.ID)
				.collect(Collectors.toList());
		if (idFields.size() == 2) {
			ResourceField field0 = idFields.get(0);
			ResourceField field1 = idFields.get(1);

			BeanInformation beanInformation = BeanInformation.get(resourceClass);
			BeanAttributeInformation attr0 = beanInformation.getAttribute(field0.getUnderlyingName());
			BeanAttributeInformation attr1 = beanInformation.getAttribute(field1.getUnderlyingName());

			boolean jsonApiId0 = attr0.getAnnotation(JsonApiId.class).isPresent();
			boolean jsonApiId1 = attr1.getAnnotation(JsonApiId.class).isPresent();
			if (jsonApiId0 && !jsonApiId1) {
				((ResourceFieldImpl) field1).setResourceFieldType(ResourceFieldType.ATTRIBUTE);
				((ResourceFieldImpl) field1).setJsonName(getJsonName(attr1, ResourceFieldType.ATTRIBUTE)); // undo enforce "id" name

			}
			else if (!jsonApiId0 && jsonApiId1) {
				((ResourceFieldImpl) field0).setResourceFieldType(ResourceFieldType.ATTRIBUTE);
				((ResourceFieldImpl) field0).setJsonName(getJsonName(attr0, ResourceFieldType.ATTRIBUTE)); // undo enforce "id" name
			}
		}
	}

	@Override
	public String getResourceType(Class<?> entityClass) {
		JsonApiResource annotation1 = entityClass.getAnnotation(JsonApiResource.class);
		if (annotation1 != null) {
			return annotation1.type();
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

	@Override
	public String getResourcePath(Class<?> entityClass) {
		JsonApiResource annotation1 = entityClass.getAnnotation(JsonApiResource.class);
		if (annotation1 != null && !"".equals(annotation1.resourcePath())) {
			return annotation1.resourcePath();
		}

		return getResourceType(entityClass);
	}

	@Override
	protected LookupIncludeBehavior getDefaultLookupIncludeBehavior(BeanAttributeInformation attr) {
		// related repositories should lookup, we ignore the hibernate proxies
		return LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
	}


	@Override
	protected RelationshipRepositoryBehavior getDefaultRelationshipRepositoryBehavior(BeanAttributeInformation attributeDesc) {
		Optional<OneToOne> oneToOne = attributeDesc.getAnnotation(OneToOne.class);
		Optional<OneToMany> oneToMany = attributeDesc.getAnnotation(OneToMany.class);
		Optional<ManyToOne> manyToOne = attributeDesc.getAnnotation(ManyToOne.class);
		Optional<ManyToMany> manyToMany = attributeDesc.getAnnotation(ManyToMany.class);
		if (oneToOne.isPresent() || manyToOne.isPresent() || oneToMany.isPresent() || manyToMany.isPresent()) {
			Optional<String> mappedBy = getMappedBy(attributeDesc);
			if (mappedBy.isPresent() && mappedBy.get().length() > 0) {
				return RelationshipRepositoryBehavior.FORWARD_OPPOSITE;
			}
			return RelationshipRepositoryBehavior.FORWARD_OWNER;
		}
		return RelationshipRepositoryBehavior.DEFAULT;
	}

	@Override
	public void init(ResourceInformationProviderContext context) {
		super.init(context);
	}

	@Override
	public int getPriority() {
		return DefaultResourceInformationProvider.PRIORITY - 10;
	}

	class JpaOptimisticLockingValidator implements ResourceValidator {

		private MetaDataObject jpaMeta;

		public JpaOptimisticLockingValidator(MetaDataObject jpaMeta) {
			this.jpaMeta = jpaMeta;
		}

		@Override
		public void validate(Object entity, Document requestDocument) {
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
	}

	/**
	 * Specialized ID handling to take care of embeddables and compound
	 * primary keys.
	 */
	class JpaIdMapper implements StringMapper {

		private MetaDataObject jpaMeta;

		public JpaIdMapper(MetaDataObject jpaMeta) {
			this.jpaMeta = jpaMeta;
		}


		@Override
		public String toString(Object input) {
			return jpaMeta.getPrimaryKey().toKeyString(input);
		}

		@Override
		public Object parse(String input) {
			MetaKey primaryKey = jpaMeta.getPrimaryKey();
			MetaAttribute attr = primaryKey.getUniqueElement();
			return fromKeyString(attr.getType(), input);
		}

		private Object fromKeyString(MetaType type, String idString) {
			// => support compound keys with unique ids
			if (type instanceof MetaDataObject && !context.getTypeParser().supports(type.getImplementationClass())) {
				return parseEmbeddableString((MetaDataObject) type, idString);
			}
			return context.getTypeParser().parse(idString, (Class) type.getImplementationClass());
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
	}

}
