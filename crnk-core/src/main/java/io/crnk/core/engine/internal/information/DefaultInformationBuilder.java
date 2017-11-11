package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultInformationBuilder implements InformationBuilder {

	private final TypeParser typeParser;

	public RelationshipRepository createRelationshipRepository(String targetResourceType) {
		return createRelationshipRepository(null, targetResourceType);
	}

	@Override
	public Field createResourceField() {
		return new DefaultField();
	}

	@Override
	public RelationshipRepository createRelationshipRepository(String sourceResourceType, String targetResourceType) {
		DefaultRelationshipRepository repository = new DefaultRelationshipRepository();
		repository.sourceResourceType = sourceResourceType;
		repository.targetResourceType = targetResourceType;
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

		private String sourceResourceType;

		private String targetResourceType;

		private RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);

		public void setAccess(RepositoryMethodAccess access) {
			this.access = access;
		}

		@Override
		public RelationshipRepositoryInformation build() {
			return new RelationshipRepositoryInformationImpl(null, sourceResourceType, targetResourceType, access);
		}
	}

	public class DefaultResourceRepository implements ResourceRepository {

		private ResourceInformation resourceInformation;

		private Map<String, RepositoryAction> actions = new HashMap<>();

		private RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);

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

		public ResourceInformation build() {

			List<ResourceField> fieldImpls = new ArrayList<>();
			for (DefaultField field : fields) {
				fieldImpls.add(field.build());
			}

			return new ResourceInformation(typeParser, resourceClass, resourceType, superResourceType,
					fieldImpls);
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

		private ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true);

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
					access);
			if (accessor != null) {
				impl.setAccessor(accessor);
			}
			return impl;
		}


		public DefaultField name(String name) {
			this.jsonName = name;
			this.underlyingName = name;
			return this;
		}


		public DefaultField jsonName(String jsonName) {
			this.jsonName = jsonName;
			return this;
		}

		public DefaultField underlyingName(String underlyingName) {
			this.underlyingName = underlyingName;
			return this;
		}


		public DefaultField type(Class<?> type) {
			this.type = type;
			if (this.genericType == null) {
				this.genericType = type;
			}
			return this;
		}

		public DefaultField genericType(Type genericType) {
			this.genericType = genericType;
			return this;
		}

		public DefaultField serializeType(SerializeType serializeType) {
			this.serializeType = serializeType;
			return this;
		}

		public DefaultField oppositeResourceType(String oppositeResourceType) {
			this.oppositeResourceType = oppositeResourceType;
			return this;
		}

		public DefaultField lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior) {
			this.lookupIncludeBehavior = lookupIncludeBehavior;
			return this;
		}

		public DefaultField fieldType(ResourceFieldType fieldType) {
			this.fieldType = fieldType;
			return this;
		}

		public DefaultField oppositeName(String oppositeName) {
			this.oppositeName = oppositeName;
			return this;
		}

		public DefaultField accessor(ResourceFieldAccessor accessor) {
			this.accessor = accessor;
			return this;
		}

		public DefaultField access(ResourceFieldAccess access) {
			this.access = access;
			return this;
		}
	}


	public DefaultInformationBuilder(TypeParser typeParser) {
		this.typeParser = typeParser;
	}
}
