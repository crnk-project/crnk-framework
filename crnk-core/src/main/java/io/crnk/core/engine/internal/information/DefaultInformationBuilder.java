package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceValidator;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.parser.StringMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultInformationBuilder implements InformationBuilder {

	private final TypeParser typeParser;

	@Override
	public Field createResourceField() {
		return new DefaultField();
	}

	@Override
	public RelationshipRepository createRelationshipRepository(String sourceResourceType, String targetResourceType) {
		RelationshipMatcher matcher = new RelationshipMatcher();
		matcher.rule().target(targetResourceType).source(sourceResourceType).add();
		return createRelationshipRepository(matcher);
	}

	@Override
	public RelationshipRepository createRelationshipRepository(RelationshipMatcher matcher) {
		DefaultRelationshipRepository repository = new DefaultRelationshipRepository();
		repository.matcher = matcher;
		return repository;
	}


	@Override
	public ResourceRepository createResourceRepository() {
		return new DefaultResourceRepository();
	}

	@Override
	public Resource createResource(Class<?> resourceClass, String resourceType) {
		DefaultResource resource = new DefaultResource();
		resource.resourceClass(resourceClass);
		resource.resourceType(resourceType);
		return resource;
	}

	public class DefaultRelationshipRepository implements RelationshipRepository {

		private RelationshipMatcher matcher;

		private RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);

		public void setAccess(RepositoryMethodAccess access) {
			this.access = access;
		}

		@Override
		public RelationshipRepositoryInformation build() {
			return new RelationshipRepositoryInformationImpl(matcher, access);
		}
	}

	public class DefaultResourceRepository implements ResourceRepository {

		private ResourceInformation resourceInformation;

		private Map<String, RepositoryAction> actions = new HashMap<>();

		private RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);

		@Override
		public void from(ResourceRepositoryInformation information) {
			actions.putAll(information.getActions());
			access = information.getAccess();
			if (information.getResourceInformation().isPresent()) {
				resourceInformation = information.getResourceInformation().get();
			}
		}

		@Override
		public void setResourceInformation(ResourceInformation resourceInformation) {
			this.resourceInformation = resourceInformation;
		}

		@Override
		public void setAccess(RepositoryMethodAccess access) {
			this.access = access;
		}

		public ResourceRepositoryInformation build() {
			return new ResourceRepositoryInformationImpl(resourceInformation.getResourceType(),
					resourceInformation, actions, access);
		}
	}

	public class DefaultResource implements Resource {

		private List<DefaultField> fields = new ArrayList<>();

		private Class<?> resourceClass;

		private String resourceType;

		private String superResourceType;

		private StringMapper idStringMapper;

		private ResourceValidator validator;

		private PagingBehavior pagingBehavior;

		@Override
		public void from(ResourceInformation information) {
			resourceClass = information.getResourceClass();
			resourceType = information.getResourceType();
			superResourceType = information.getSuperResourceType();
			idStringMapper = information.getIdStringMapper();
			validator = information.getValidator();
			for (ResourceField fromField : information.getFields()) {
				DefaultField field = new DefaultField();
				field.from(fromField);
				fields.add(field);
			}
			pagingBehavior = information.getPagingBehavior();
		}

		@Override
		public DefaultField addField(String name, ResourceFieldType type, Class<?> clazz) {
			DefaultField field = new DefaultField();
			field.jsonName(name);
			field.underlyingName(name);
			field.type(clazz);
			field.genericType(clazz);
			field.fieldType(type);
			fields.add(field);
			return field;
		}

		public DefaultResource resourceClass(Class<?> resourceClass) {
			this.resourceClass = resourceClass;
			return this;
		}

		public DefaultResource resourceType(String resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public DefaultResource superResourceType(String superResourceType) {
			this.superResourceType = superResourceType;
			return this;
		}

		@Override
		public Resource pagingBehavior(PagingBehavior pagingBehavior) {
			this.pagingBehavior = pagingBehavior;

			return this;
		}

		public ResourceInformation build() {

			List<ResourceField> fieldImpls = new ArrayList<>();
			for (DefaultField field : fields) {
				fieldImpls.add(field.build());
			}

			ResourceInformation information = new ResourceInformation(typeParser, resourceClass, resourceType, superResourceType,
					fieldImpls, pagingBehavior);
			if (validator != null) {
				information.setValidator(validator);
			}
			if (idStringMapper != null) {
				information.setIdStringMapper(idStringMapper);
			}
			return information;
		}
	}

	public class DefaultField implements InformationBuilder.Field {

		private String jsonName;

		private String underlyingName;

		private Class<?> type;

		private Type genericType;

		private String oppositeResourceType = null;

		private LookupIncludeBehavior lookupIncludeBehavior = LookupIncludeBehavior.DEFAULT;

		private ResourceFieldType fieldType = ResourceFieldType.ATTRIBUTE;

		private SerializeType serializeType = SerializeType.LAZY;

		private String oppositeName;

		private ResourceFieldAccessor accessor;

		private String idName;

		private Class idType;

		private ResourceFieldAccessor idAccessor;

		private ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true);

		private RelationshipRepositoryBehavior relationshipRepositoryBehavior = RelationshipRepositoryBehavior.DEFAULT;

		@Override
		public void from(ResourceField field) {
			jsonName = field.getJsonName();
			underlyingName = field.getUnderlyingName();
			type = field.getType();
			genericType = field.getGenericType();
			fieldType = field.getResourceFieldType();
			accessor = field.getAccessor();
			access = field.getAccess();
			serializeType = field.getSerializeType();
			if (fieldType == ResourceFieldType.RELATIONSHIP) {
				relationshipRepositoryBehavior = field.getRelationshipRepositoryBehavior();
				oppositeResourceType = field.getOppositeResourceType();
				lookupIncludeBehavior = field.getLookupIncludeAutomatically();
				oppositeName = field.getOppositeName();
				if (field.hasIdField()) {
					idName = field.getIdName();
					idType = field.getIdType();
					idAccessor = field.getIdAccessor();
				}
			}
		}


		public ResourceField build() {

			if (oppositeResourceType == null && fieldType == ResourceFieldType.RELATIONSHIP) {
				// TODO consider separating informationBuilder from resourceType extraction
				Class<?> elementType = ClassUtils.getRawType(ClassUtils.getElementType(genericType));
				JsonApiResource annotation = elementType.getAnnotation(JsonApiResource.class);
				if (annotation != null) {
					oppositeResourceType = annotation.type();
				}
			}

			ResourceFieldImpl impl = new ResourceFieldImpl(jsonName, underlyingName, fieldType, type,
					genericType, oppositeResourceType, oppositeName, serializeType,
					lookupIncludeBehavior,
					access, idName, idType, idAccessor, relationshipRepositoryBehavior);
			if (accessor != null) {
				impl.setAccessor(accessor);
			}
			return impl;
		}

		@Override
		public DefaultField name(String name) {
			this.jsonName = name;
			this.underlyingName = name;
			return this;
		}

		@Override
		public DefaultField relationshipRepositoryBehavior(
				RelationshipRepositoryBehavior relationshipRepositoryBehavior) {
			this.relationshipRepositoryBehavior = relationshipRepositoryBehavior;
			return this;
		}


		@Override
		public DefaultField jsonName(String jsonName) {
			this.jsonName = jsonName;
			return this;
		}

		@Override
		public DefaultField underlyingName(String underlyingName) {
			this.underlyingName = underlyingName;
			return this;
		}

		@Override
		public DefaultField type(Class<?> type) {
			this.type = type;
			if (this.genericType == null) {
				this.genericType = type;
			}
			return this;
		}

		@Override
		public DefaultField genericType(Type genericType) {
			this.genericType = genericType;
			if (type == null) {
				type = ClassUtils.getRawType(genericType);
			}
			return this;
		}

		@Override
		public DefaultField serializeType(SerializeType serializeType) {
			this.serializeType = serializeType;
			return this;
		}

		@Override
		public DefaultField oppositeResourceType(String oppositeResourceType) {
			this.oppositeResourceType = oppositeResourceType;
			return this;
		}

		@Override
		public DefaultField lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior) {
			this.lookupIncludeBehavior = lookupIncludeBehavior;
			return this;
		}

		@Override
		public DefaultField fieldType(ResourceFieldType fieldType) {
			this.fieldType = fieldType;
			return this;
		}

		@Override
		public DefaultField oppositeName(String oppositeName) {
			this.oppositeName = oppositeName;
			return this;
		}

		@Override
		public DefaultField accessor(ResourceFieldAccessor accessor) {
			this.accessor = accessor;
			return this;
		}

		@Override
		public DefaultField idAccessor(ResourceFieldAccessor idAccessor) {
			this.idAccessor = idAccessor;
			return this;
		}

		@Override
		public DefaultField idName(String idName) {
			this.idName = idName;
			return this;
		}

		@Override
		public DefaultField idType(Class idType) {
			this.idType = idType;
			return this;
		}


		@Override
		public DefaultField access(ResourceFieldAccess access) {
			this.access = access;
			return this;
		}
	}


	public DefaultInformationBuilder(TypeParser typeParser) {
		this.typeParser = typeParser;
	}
}
