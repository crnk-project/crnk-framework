package io.crnk.jpa.internal;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.information.resource.ResourceValidator;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceInformationProviderBase;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.StringMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.jpa.annotations.JpaResource;
import io.crnk.jpa.meta.JpaMetaProvider;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.jpa.meta.internal.JpaMetaUtils;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaType;

import javax.persistence.MappedSuperclass;
import javax.persistence.OptimisticLockException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Extracts resource information from JPA and Crnk annotations. Crnk
 * annotations take precedence.
 */
public class JpaResourceInformationProvider extends ResourceInformationProviderBase {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private final JpaMetaProvider metaProvider;

	public JpaResourceInformationProvider(PropertiesProvider propertiesProvider) {
		super(
				propertiesProvider,
				Arrays.asList(new DefaultResourceFieldInformationProvider(), new JpaResourceFieldInformationProvider(),
						new JacksonResourceFieldInformationProvider()));

		metaProvider = new JpaMetaProvider((Set) Collections.emptySet());
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(metaProvider);
		lookup.initialize();

		PreconditionUtil.assertNotNull("must not be null", metaProvider);
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		// needs to be configured for being exposed
		if (resourceClass.getAnnotation(JpaResource.class) != null) {
			return true;
		}

		if (JpaMetaUtils.isJpaType(resourceClass)) {
			// needs to be an entity
			MetaElement meta = metaProvider.discoverMeta(resourceClass);
			if (meta instanceof MetaEntity) {
				MetaEntity metaEntity = (MetaEntity) meta;
				MetaKey primaryKey = metaEntity.getPrimaryKey();
				return primaryKey != null && primaryKey.getElements().size() == 1;
			}
			else {
				// note that DTOs cannot be handled here
				return meta instanceof MetaJpaDataObject;
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

		List<ResourceField> fields = getResourceFields(resourceClass);

		Class<?> superclass = resourceClass.getSuperclass();
		String superResourceType = superclass != Object.class
				&& superclass.getAnnotation(MappedSuperclass.class) == null ? context.getResourceType(superclass)
				: null;

		TypeParser typeParser = context.getTypeParser();
		ResourceInformation info = new ResourceInformation(typeParser, resourceClass, resourceType, resourcePath, superResourceType,
				instanceBuilder, fields, new OffsetLimitPagingBehavior());
		info.setValidator(new JpaOptimisticLockingValidator(meta));
		info.setIdStringMapper(new JpaIdMapper(meta));
		return info;
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

	@Override
	public String getResourcePath(Class<?> entityClass) {
		return null;
	}

	@Override
	protected LookupIncludeBehavior getDefaultLookupIncludeBehavior() {
		// related repositories should lookup, we ignore the hibernate proxies
		return LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
	}

	@Override
	public void init(ResourceInformationProviderContext context) {
		super.init(context);
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
			return (Serializable) fromKeyString(attr.getType(), input);
		}

		private Object fromKeyString(MetaType type, String idString) {
			// => support compound keys with unique ids
			if (type instanceof MetaDataObject) {
				return parseEmbeddableString((MetaDataObject) type, idString);
			}
			else {
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
	}

}
