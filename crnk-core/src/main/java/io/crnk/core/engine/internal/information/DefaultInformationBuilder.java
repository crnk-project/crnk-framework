package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultInformationBuilder implements InformationBuilder {

	private final TypeParser typeParser;

	@Override
	public RelationshipRepository createRelationshipRepository(ResourceInformation sourceInformation, ResourceInformation targetInformation) {
		DefaultRelationshipRepository repository = new DefaultRelationshipRepository();
		repository.sourceResourceInformation = sourceInformation;
		repository.targetResourceInformation = targetInformation;
		return repository;
	}

	@Override
	public ResourceRepository createResourceRepository(Class<?> resourceClass, String resourceType) {
		DefaultResource resource = new DefaultResource();
		resource.resourceClass(resourceClass);
		resource.resourceType(resourceType);

		DefaultResourceRepository repository = new DefaultResourceRepository();
		repository.resource = resource;
		return repository;
	}

	public class DefaultRelationshipRepository implements RelationshipRepository {

		private ResourceInformation sourceResourceInformation;

		private ResourceInformation targetResourceInformation;


		@Override
		public RelationshipRepositoryInformation build() {
			return new RelationshipRepositoryInformationImpl(sourceResourceInformation, targetResourceInformation);
		}
	}

	public class DefaultResourceRepository implements ResourceRepository {

		private DefaultResource resource;

		private Map<String, RepositoryAction> actions = new HashMap<>();

		public DefaultResource resource() {
			return resource;
		}

		public ResourceRepositoryInformation build() {
			ResourceInformation resourceInformation = resource.build();

			return new ResourceRepositoryInformationImpl(resourceInformation.getResourceType(),
					resourceInformation, actions);
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

		public void resourceClass(Class<?> resourceClass) {
			this.resourceClass = resourceClass;
		}

		public void resourceType(String resourceType) {
			this.resourceType = resourceType;
		}

		public void superResourceType(String superResourceType) {
			this.superResourceType = superResourceType;
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

		private boolean lazy = true;

		private String oppositeResourceType = null;

		private LookupIncludeBehavior lookupIncludeBehavior = LookupIncludeBehavior.NONE;

		private boolean includeByDefault = false;

		private ResourceFieldType fieldType = ResourceFieldType.ATTRIBUTE;

		private String oppositeName;

		private ResourceFieldAccessor accessor;

		private ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true);

		public ResourceField build() {
			return new ResourceFieldImpl(jsonName, underlyingName, fieldType, type,
					genericType, oppositeResourceType, oppositeName, lazy,
					includeByDefault, lookupIncludeBehavior,
					access);
		}

		public void jsonName(String jsonName) {
			this.jsonName = jsonName;
		}

		public void underlyingName(String underlyingName) {
			this.underlyingName = underlyingName;
		}


		public void type(Class<?> type) {
			this.type = type;
		}

		public void genericType(Type genericType) {
			this.genericType = genericType;
		}

		public void lazy(boolean lazy) {
			this.lazy = lazy;
		}

		public void oppositeResourceType(String oppositeResourceType) {
			this.oppositeResourceType = oppositeResourceType;
		}

		public void lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior) {
			this.lookupIncludeBehavior = lookupIncludeBehavior;
		}

		public void includeByDefault(boolean includeByDefault) {
			this.includeByDefault = includeByDefault;
		}

		public void fieldType(ResourceFieldType fieldType) {
			this.fieldType = fieldType;
		}

		public void setOppositeName(String oppositeName) {
			this.oppositeName = oppositeName;
		}

		public void setAccessor(ResourceFieldAccessor accessor) {
			this.accessor = accessor;
		}

		public void setAccess(ResourceFieldAccess access) {
			this.access = access;
		}
	}


	public DefaultInformationBuilder(TypeParser typeParser) {
		this.typeParser = typeParser;
	}
}
