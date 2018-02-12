package io.crnk.core.engine.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.contributor.ResourceFieldContributorContext;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.internal.DefaultRepositoryInformationProviderContext;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.foward.ForwardingDirection;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.crnk.legacy.registry.AnnotatedResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRegistryEntryBuilder implements RegistryEntryBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegistryEntryBuilder.class);

	private final DefaultInformationBuilder informationBuilder;

	private ModuleRegistry moduleRegistry;

	private DefaultResourceRepository resourceRepository;

	private Map<String, DefaultRelationshipRepository> relationshipRepositoryMap = new HashMap<>();

	private InformationBuilder.Resource resource;

	public DefaultRegistryEntryBuilder(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.informationBuilder = moduleRegistry.getInformationBuilder();
	}


	class DefaultResourceRepository implements ResourceRepository {

		private Object instance;

		private InformationBuilder.ResourceRepository information;

		public DefaultResourceRepository() {
			this.information = informationBuilder.createResourceRepository();
		}

		@Override
		public InformationBuilder.ResourceRepository information() {
			return information;
		}

		@Override
		public void instance(Object instance) {
			this.instance = instance;
		}
	}

	class DefaultRelationshipRepository implements RelationshipRepository {

		private final String fieldName;

		private InformationBuilder.RelationshipRepository information;

		private Object instance;


		public DefaultRelationshipRepository(String fieldName) {
			this.fieldName = fieldName;
			this.information = informationBuilder.createRelationshipRepository((RelationshipMatcher) null);
		}

		@Override
		public InformationBuilder.RelationshipRepository information() {
			return information;
		}

		@Override
		public void instance(Object instance) {
			this.instance = instance;
		}
	}

	@Override
	public void fromImplemenation(Object repository) {
		RepositoryInformationProvider repositoryInformationBuilder = moduleRegistry.getRepositoryInformationBuilder();

		RepositoryInformationProviderContext builderContext = new DefaultRepositoryInformationProviderContext(moduleRegistry);
		ResourceRepositoryInformation repositoryInformation =
				(ResourceRepositoryInformation) repositoryInformationBuilder.build(repository, builderContext);
		ResourceInformation resourceInformation = repositoryInformation.getResourceInformation().get();

		resource().from(resourceInformation);
		resourceRepository().information().from(repositoryInformation);
		resourceRepository().instance(repository);
	}


	@Override
	public ResourceRepository resourceRepository() {
		if (resourceRepository == null) {
			resourceRepository = new DefaultResourceRepository();
		}
		return resourceRepository;
	}

	@Override
	public InformationBuilder.Resource resource() {
		if (resource == null) {
			resource = informationBuilder.createResource(null, null);
		}
		return resource;
	}

	@Override
	public RelationshipRepository relationshipRepositoryForField(String fieldName) {
		DefaultRelationshipRepository repository = relationshipRepositoryMap.get(fieldName);
		if (repository == null) {
			repository = new DefaultRelationshipRepository(null);
			relationshipRepositoryMap.put(fieldName, repository);
		}
		return repository;
	}

	@Override
	public RegistryEntry build() {
		ResourceInformation resourceInformation = buildResource();
		ResourceEntry resourceEntry = buildResourceRepository(resourceInformation);
		Map<ResourceField, ResponseRelationshipEntry> relationshipEntries = buildRelationships(resourceInformation);

		RegistryEntry entry =
				new RegistryEntry(resourceEntry, relationshipEntries);
		entry.initialize(moduleRegistry);
		return entry;
	}


	private Map<ResourceField, ResponseRelationshipEntry> buildRelationships(ResourceInformation resourceInformation) {
		for (String relationshipName : relationshipRepositoryMap.keySet()) {
			if (resourceInformation.findFieldByUnderlyingName(relationshipName) == null) {
				throw new ResourceFieldNotFoundException("failed to find relationship field '" + relationshipName + "' to setup "
						+ "registered relationship repository");
			}

		}


		Map<ResourceField, ResponseRelationshipEntry> map = new HashMap<>();
		for (ResourceField relationshipField : resourceInformation.getRelationshipFields()) {

			ResponseRelationshipEntry relationshipEntry = null;

			// check for local definition
			DefaultRelationshipRepository repository = relationshipRepositoryMap.get(relationshipField.getUnderlyingName());
			if (repository != null) {
				RelationshipRepositoryInformation relationshipInformation = repository.information.build();
				relationshipEntry = setupRelationship(relationshipField, relationshipInformation, repository.instance);
			}

			// check for match
			if (relationshipEntry == null) {
				relationshipEntry = findRelationshipMatch(relationshipField);
			}

			// check for implicit
			if (relationshipEntry == null) {
				relationshipEntry = setupImplicitRelationshipRepository(relationshipField);
			}

			if (relationshipEntry != null) {
				map.put(relationshipField, relationshipEntry);
			}
			else {
				LOGGER.warn("no relationship repository found for " + resourceInformation.getResourceType() + "." +
						relationshipField.getUnderlyingName());
			}
		}
		return map;
	}

	private ResponseRelationshipEntry findRelationshipMatch(ResourceField relationshipField) {
		Collection<Object> repositories = moduleRegistry.getRepositories();
		RepositoryInformationProvider repositoryInformationBuilder = moduleRegistry.getRepositoryInformationBuilder();

		ResponseRelationshipEntry matchedEntry = null;


		for (Object repository : repositories) {
			RepositoryInformation repositoryInformation =
					repositoryInformationBuilder.build(repository, new
							DefaultRepositoryInformationProviderContext(moduleRegistry));

			if (repositoryInformation instanceof RelationshipRepositoryInformation) {
				RelationshipRepositoryInformation relationshipRepositoryInformation =
						(RelationshipRepositoryInformation) repositoryInformation;
				RelationshipMatcher matcher = relationshipRepositoryInformation.getMatcher();
				if (matcher.matches(relationshipField)) {
					if (matchedEntry != null) {
						throw new IllegalStateException("multiple repositories for " + relationshipField + ": " + repository +
								", " + matchedEntry);
					}
					matcher.matches(relationshipField);
					matchedEntry = setupRelationship(relationshipField, relationshipRepositoryInformation, repository);
				}
			}
		}
		return matchedEntry;
	}

	private ResourceInformation buildResource() {
		ResourceInformation resourceInformation = resource.build();
		contributeFields(resourceInformation);
		return resourceInformation;
	}

	private void contributeFields(ResourceInformation resourceInformation) {
		// TODO make service discovery the primary target to resolve all objects => wrapped it with module
		List<ResourceFieldContributor> contributors = new ArrayList<>();
		contributors.addAll(moduleRegistry.getServiceDiscovery().getInstancesByType(ResourceFieldContributor.class));
		moduleRegistry.getRepositories().stream()
				.filter(it -> it instanceof ResourceFieldContributor && !contributors.contains(it))
				.forEach(it -> contributors.add((ResourceFieldContributor) it));

		for (ResourceFieldContributor contributor : contributors) {
			List<ResourceField> contributedFields = contributor.getResourceFields(new ResourceFieldContributorContext() {
				@Override
				public InformationBuilder getInformationBuilder() {
					return new DefaultInformationBuilder(moduleRegistry.getTypeParser());
				}
			});
			List<ResourceField> fields = new ArrayList<>();
			fields.addAll(resourceInformation.getFields());
			fields.addAll(contributedFields);
			resourceInformation.setFields(fields);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResourceEntry buildResourceRepository(ResourceInformation resourceInformation) {
		resourceRepository.information().setResourceInformation(resourceInformation);
		ResourceRepositoryInformation repositoryInformation = resourceRepository.information().build();

		Object instance = resourceRepository.instance;
		final Object decoratedRepository = decorateRepository(instance);
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, instance.getClass()) {

			@Override
			public Object buildRepository() {
				return decoratedRepository;
			}
		};

		if (ClassUtils.getAnnotation(decoratedRepository.getClass(), JsonApiResourceRepository.class).isPresent()) {
			return new AnnotatedResourceEntry(repositoryInstanceBuilder, repositoryInformation);
		}
		else {
			return new DirectResponseResourceEntry(repositoryInstanceBuilder, repositoryInformation);
		}
	}

	private ResponseRelationshipEntry setupImplicitRelationshipRepository(ResourceField relationshipField) {
		RelationshipRepositoryBehavior behavior = relationshipField.getRelationshipRepositoryBehavior();
		if (behavior == RelationshipRepositoryBehavior.DEFAULT) {
			if (relationshipField.hasIdField()
					|| relationshipField.getLookupIncludeAutomatically() == LookupIncludeBehavior.NONE) {
				behavior = RelationshipRepositoryBehavior.FORWARD_OWNER;
			}
			else {
				behavior = RelationshipRepositoryBehavior.CUSTOM;
			}
		}
		if (behavior == RelationshipRepositoryBehavior.IMPLICIT_FROM_OWNER) {
			behavior = RelationshipRepositoryBehavior.FORWARD_OWNER;
		}
		if (behavior == RelationshipRepositoryBehavior.IMPLICIT_GET_OPPOSITE_MODIFY_OWNER) {
			behavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER;
		}

		if (behavior != RelationshipRepositoryBehavior.CUSTOM) {
			ResourceInformation sourceInformation = relationshipField.getParentResourceInformation();

			ResourceFieldAccess fieldAccess = relationshipField.getAccess();

			RepositoryMethodAccess access = new RepositoryMethodAccess(fieldAccess.isPostable(), fieldAccess.isPatchable(),
					fieldAccess.isReadable(), fieldAccess.isPatchable());

			RelationshipMatcher matcher = new RelationshipMatcher().rule().field(relationshipField).add();

			RelationshipRepositoryInformationImpl implicitRepoInformation =
					new RelationshipRepositoryInformationImpl(matcher, access);

			ForwardingRelationshipRepository repository;
			if (behavior == RelationshipRepositoryBehavior.FORWARD_OWNER) {
				repository = new ForwardingRelationshipRepository(sourceInformation.getResourceType(), matcher,
						ForwardingDirection.OWNER, ForwardingDirection.OWNER);
			}
			else if (behavior == RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER) {
				repository = new ForwardingRelationshipRepository(sourceInformation.getResourceType(), matcher,
						ForwardingDirection.OPPOSITE, ForwardingDirection.OWNER);
			}
			else {
				PreconditionUtil.assertEquals("unknown behavior", RelationshipRepositoryBehavior
						.FORWARD_OPPOSITE, behavior);
				repository = new ForwardingRelationshipRepository(sourceInformation.getResourceType(), matcher,
						ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
			}
			repository.setResourceRegistry(moduleRegistry.getResourceRegistry());
			return setupRelationship(relationshipField, implicitRepoInformation, repository);
		}
		else {
			return null;
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object decorateRepository(Object repository) {
		Object decoratedRepository = repository;
		List<RepositoryDecoratorFactory> repositoryDecorators = moduleRegistry.getRepositoryDecoratorFactories();
		for (RepositoryDecoratorFactory repositoryDecorator : repositoryDecorators) {
			Decorator decorator = null;
			if (decoratedRepository instanceof RelationshipRepositoryV2) {
				decorator = repositoryDecorator.decorateRepository((RelationshipRepositoryV2) decoratedRepository);
			}
			else if (decoratedRepository instanceof ResourceRepositoryV2) {
				decorator = repositoryDecorator.decorateRepository((ResourceRepositoryV2) decoratedRepository);
			}
			if (decorator != null) {
				decorator.setDecoratedObject(decoratedRepository);
				decoratedRepository = decorator;
			}
		}
		return decoratedRepository;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResponseRelationshipEntry setupRelationship(
			final ResourceField relationshipField,
			RelationshipRepositoryInformation relationshipRepositoryInformation,
			Object relRepository) {

		final Object decoratedRepository = decorateRepository(relRepository);
		RepositoryInstanceBuilder<Object> relationshipInstanceBuilder =
				new RepositoryInstanceBuilder<Object>(null, (Class) relRepository.getClass()) {

					@Override
					public Object buildRepository() {
						return decoratedRepository;
					}
				};

		if (ClassUtils.getAnnotation(relRepository.getClass(), JsonApiRelationshipRepository.class).isPresent()) {
			return new AnnotatedRelationshipEntryBuilder(moduleRegistry, relationshipInstanceBuilder);
		}
		else {
			final String targetResourceType = relationshipField.getOppositeResourceType();
			return new DirectResponseRelationshipEntry(relationshipInstanceBuilder) {

				@Override
				public String getTargetResourceType() {
					return targetResourceType;
				}
			};
		}
	}

}
