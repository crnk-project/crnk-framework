package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.document.Resource;
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
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.internal.DefaultRepositoryInformationProviderContext;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.foward.ForwardingDirection;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRegistryEntryBuilder implements RegistryEntryBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegistryEntryBuilder.class);

    public static boolean FAIL_ON_MISSING_REPOSITORY = true;

    /**
     * @deprecated find better solution
     */
    @Deprecated
    public static boolean WARN_MISSING_RELATIONSHIP_REPOSITORIES = true;

    private final DefaultInformationBuilder informationBuilder;

    private ModuleRegistry moduleRegistry;

    private DefaultResourceRepository resourceRepository;

    private Map<String, DefaultRelationshipRepository> relationshipRepositoryMap = new HashMap<>();

    private InformationBuilder.ResourceInformationBuilder resource;

    public DefaultRegistryEntryBuilder(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
        this.informationBuilder = moduleRegistry.getInformationBuilder();
    }


    class DefaultResourceRepository implements ResourceRepositoryEntryBuilder {

        private Object instance;

        private InformationBuilder.ResourceRepositoryInformationBuilder information;

        public DefaultResourceRepository() {
            this.information = informationBuilder.createResourceRepository();
        }

        @Override
        public InformationBuilder.ResourceRepositoryInformationBuilder information() {
            return information;
        }

        @Override
        public void instance(Object instance) {
            this.instance = instance;
        }
    }

    class DefaultRelationshipRepository implements RelationshipRepositoryEntryBuilder {

        private final String fieldName;

        private InformationBuilder.RelationshipRepositoryInformationBuilder information;

        private Object instance;


        public DefaultRelationshipRepository(String fieldName) {
            this.fieldName = fieldName;
            this.information = informationBuilder.createRelationshipRepository(null);
        }

        @Override
        public InformationBuilder.RelationshipRepositoryInformationBuilder information() {
            return information;
        }

        @Override
        public void instance(Object instance) {
            this.instance = instance;
        }
    }

    @Override
    public void fromImplementation(Object repository) {
        RepositoryInformationProvider repositoryInformationBuilder = moduleRegistry.getRepositoryInformationBuilder();

        RepositoryInformationProviderContext builderContext = new DefaultRepositoryInformationProviderContext(moduleRegistry);
        RepositoryInformation repositoryInformation = repositoryInformationBuilder.build(repository, builderContext);
        if (repositoryInformation instanceof ResourceRepositoryInformation) {
            // consider relationship repositories without resource repositories in the future?
            ResourceRepositoryInformation resourceRepositoryInformation = (ResourceRepositoryInformation) repositoryInformation;
            ResourceInformation resourceInformation = resourceRepositoryInformation.getResourceInformation().get();
            resource().from(resourceInformation);
            resourceRepository().information().from(resourceRepositoryInformation);
            resourceRepository().instance(repository);
        }
    }

    @Override
    public ResourceRepositoryEntryBuilder resourceRepository() {
        if (resourceRepository == null) {
            resourceRepository = new DefaultResourceRepository();
        }
        return resourceRepository;
    }

    @Override
    public InformationBuilder.ResourceInformationBuilder resource() {
        if (resource == null) {
            resource = informationBuilder.createResource(null, null, null);
        }
        return resource;
    }

    @Override
    public RelationshipRepositoryEntryBuilder relationshipRepositoryForField(String fieldName) {
        DefaultRelationshipRepository repository = relationshipRepositoryMap.get(fieldName);
        if (repository == null) {
            repository = new DefaultRelationshipRepository(null);
            relationshipRepositoryMap.put(fieldName, repository);
        }
        return repository;
    }

    @Override
    public RegistryEntry build() {
        if (resource == null) {
            return null; // repositories without resource repo not supported (yet?)
        }
        ResourceInformation resourceInformation = buildResource();

        ResourceRepositoryAdapter resourceRepositoryAdapter = buildResourceRepositoryAdapter(resourceInformation);
        Map<ResourceField, RelationshipRepositoryAdapter> relationshipEntries =
                buildRelationshipAdapters(resourceInformation);
        return new RegistryEntryImpl(resourceInformation, resourceRepositoryAdapter, relationshipEntries, moduleRegistry);
    }


    private void checkRelationshipNaming(ResourceInformation resourceInformation) {
        for (String relationshipName : relationshipRepositoryMap.keySet()) {
            if (resourceInformation.findFieldByUnderlyingName(relationshipName) == null) {
                throw new ResourceFieldNotFoundException("failed to find relationship field '" + relationshipName + "' to setup "
                        + "registered relationship repository");
            }

        }

    }

    private Map<ResourceField, RelationshipRepositoryAdapter> buildRelationshipAdapters(ResourceInformation resourceInformation) {
        checkRelationshipNaming(resourceInformation);

        Map<ResourceField, RelationshipRepositoryAdapter> map = new HashMap<>();
        for (ResourceField relationshipField : resourceInformation.getRelationshipFields()) {
            MatchedRelationship relationshipEntry = findMatchedRelationship(relationshipField);
            if (relationshipEntry != null) {
                map.put(relationshipField, relationshipEntry.getAdapter());
            } else {
                // does only happen if checking is disabled in general (currently just crnk-core tests)
                LOGGER.warn("{}.{}: no relationship repository found", toShortName(resourceInformation), relationshipField.getUnderlyingName());
            }
        }
        return map;
    }

    private MatchedRelationship findMatchedRelationship(ResourceField relationshipField) {
        MatchedRelationship match = null;

        // check for local definition
        DefaultRelationshipRepository repository = relationshipRepositoryMap.get(relationshipField.getUnderlyingName());
        if (repository != null) {
            RelationshipRepositoryInformation relationshipInformation = repository.information.build();
            match = new MatchedRelationship(relationshipField, relationshipInformation, repository.instance);

            ResourceInformation sourceInformation = relationshipField.getResourceInformation();
            LOGGER.debug("{}.{}: using configured relationship repository: {}",
                    toShortName(sourceInformation), relationshipField.getUnderlyingName(), match);
            ((ResourceFieldImpl) relationshipField).setRelationshipRepositoryBehavior(RelationshipRepositoryBehavior.CUSTOM);
        }

        // check for match
        if (match == null) {
            match = findRelationshipMatch(relationshipField);
            if (match != null) {
                ResourceInformation sourceInformation = relationshipField.getResourceInformation();
                ((ResourceFieldImpl) relationshipField).setRelationshipRepositoryBehavior(RelationshipRepositoryBehavior.CUSTOM);
                LOGGER.debug("{}.{}: found matching relationship repository: {}",
                        toShortName(sourceInformation), relationshipField.getUnderlyingName(), match);
            }
        }

        // check for implicit
        if (match == null) {
            match = setupForwardingRepository(relationshipField);
        }


        LookupIncludeBehavior lookupIncludeBehavior = relationshipField.getLookupIncludeBehavior();
        if (lookupIncludeBehavior == LookupIncludeBehavior.DEFAULT) {
            if (relationshipField.hasIdField()) {
                LOGGER.debug("{}.{}: relationId field enforces LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL to resolve resource from id", toShortName(relationshipField.getResourceInformation()), relationshipField.getUnderlyingName());
                ((ResourceFieldImpl) relationshipField).setLookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
            } else if (relationshipField.getRelationshipRepositoryBehavior() == RelationshipRepositoryBehavior.FORWARD_OPPOSITE) {
                LOGGER.debug("{}.{}: RelationshipRepositoryBehavior.FORWARD_OPPOSITE enforces LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL to resolve relationship from opposite side", toShortName(relationshipField.getResourceInformation()), relationshipField.getUnderlyingName());
                ((ResourceFieldImpl) relationshipField).setLookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
            } else if (relationshipField.getRelationshipRepositoryBehavior() == RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER) {
                LOGGER.debug("{}.{}: RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER enforces LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL to resolve relationship from opposite side", toShortName(relationshipField.getResourceInformation()), relationshipField.getUnderlyingName());
                ((ResourceFieldImpl) relationshipField).setLookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
            } else if (relationshipField.getRelationshipRepositoryBehavior() == RelationshipRepositoryBehavior.CUSTOM) {
                LOGGER.debug("{}.{}: RelationshipRepositoryBehavior.CUSTOM enforces LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL to resolve relationship from custom repository", toShortName(relationshipField.getResourceInformation()), relationshipField.getUnderlyingName());
                ((ResourceFieldImpl) relationshipField).setLookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
            } else {
                LOGGER.debug("{}.{}: fall back to LookupIncludeBehavior.NONE", toShortName(relationshipField.getResourceInformation()), relationshipField.getUnderlyingName());
                ((ResourceFieldImpl) relationshipField).setLookupIncludeBehavior(LookupIncludeBehavior.NONE);
            }
        }

        return match;
    }

    private MatchedRelationship findRelationshipMatch(ResourceField relationshipField) {
        MatchedRelationship matchedEntry = null;

        for (Object repository : moduleRegistry.getRepositories()) {
            RepositoryInformation repositoryInformation = moduleRegistry.getRepositoryInformation(repository);
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
                    matchedEntry = new MatchedRelationship(relationshipField, relationshipRepositoryInformation, repository);
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
        contributors.addAll(moduleRegistry.getResourceFieldContributors());
        for (Object repo : moduleRegistry.getRepositories()) {
            if (repo instanceof ResourceFieldContributor && !contributors.contains(repo)) {
                contributors.add((ResourceFieldContributor) repo);
            }
        }


        for (ResourceFieldContributor contributor : contributors) {
            List<ResourceField> contributedFields = contributor.getResourceFields(new ResourceFieldContributorContext() {
                @Override
                public ResourceInformation getResourceInformation() {
                    return resourceInformation;
                }

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


    @SuppressWarnings({"rawtypes", "unchecked"})
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

        return new DirectResponseResourceEntry(repositoryInstanceBuilder, repositoryInformation);
    }

    private MatchedRelationship setupForwardingRepository(ResourceField relationshipField) {
        ResourceInformation sourceInformation = relationshipField.getParentResourceInformation();

        RelationshipRepositoryBehavior behavior = relationshipField.getRelationshipRepositoryBehavior();
        if (behavior == RelationshipRepositoryBehavior.DEFAULT) {
            if (relationshipField.hasIdField()) {
                behavior = RelationshipRepositoryBehavior.FORWARD_OWNER;
                LOGGER.debug("{}.{}: choosing default RelationshipRepositoryBehavior: relationId field enforces FORWARD_OWNER", toShortName(sourceInformation), relationshipField.getUnderlyingName());
            } else if (relationshipField.isMappedBy()) {
                LOGGER.debug("{}.{}: choosing default RelationshipRepositoryBehavior: mappedBy enforces FORWARD_OPPOSITE", toShortName(sourceInformation), relationshipField.getUnderlyingName());
                behavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE;
            } else if (relationshipField.getLookupIncludeBehavior() == LookupIncludeBehavior.NONE) {
                LOGGER.debug("{}.{}: choosing default RelationshipRepositoryBehavior: NONE lookup behavior enforces FORWARD_OWNER", toShortName(sourceInformation), relationshipField.getUnderlyingName());
                behavior = RelationshipRepositoryBehavior.FORWARD_OWNER;
            } else if (relationshipField.getLookupIncludeBehavior() == LookupIncludeBehavior.DEFAULT) {
                LOGGER.debug("{}.{}: choosing default RelationshipRepositoryBehavior: default fallback to FORWARD_OWNER, no custom repository nor custom configuration", toShortName(sourceInformation), relationshipField.getUnderlyingName());
                behavior = RelationshipRepositoryBehavior.FORWARD_OWNER;
            } else if (FAIL_ON_MISSING_REPOSITORY) {
                throw new IllegalStateException("no relationship repository available for " + relationshipField + ", provide a custom relationship reposit implementation, add a @JsonApiRelationId field, use @JsonApiRelation.mappedBy, set @JsonApiRelation.repositoryBehavior or set @JsonApiRelation.LOOKUP to NONE");
            } else {
                return null;
            }
            ((ResourceFieldImpl) relationshipField).setRelationshipRepositoryBehavior(behavior);
        }

        if (behavior == RelationshipRepositoryBehavior.CUSTOM) {
            throw new IllegalStateException("RelationshipRepositoryBehavior.CUSTOM used for " + relationshipField + " but no implementation provided");
        }

        if (behavior == RelationshipRepositoryBehavior.FORWARD_OPPOSITE
                || behavior == RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER) {
            PreconditionUtil.verify(relationshipField.getOppositeName() != null,
                    "field %s must specify @JsonApiRelation.opposite to make use of opposite forwarding "
                            + "behavior.", relationshipField.getUnderlyingName());
        }

        ResourceFieldAccess fieldAccess = relationshipField.getAccess();
        RepositoryMethodAccess access = new RepositoryMethodAccess(fieldAccess.isPostable(), fieldAccess.isPatchable(),
                fieldAccess.isReadable(), fieldAccess.isPatchable());
        RelationshipMatcher matcher = new RelationshipMatcher().rule().field(relationshipField).add();
        RelationshipRepositoryInformationImpl implicitRepoInformation =
                new RelationshipRepositoryInformationImpl(matcher, access);

        ForwardingRelationshipRepository repository;
        if (behavior == RelationshipRepositoryBehavior.FORWARD_OWNER) {
            LOGGER.debug("{}.{}: setting up owner/owner forwarding repository", toShortName(sourceInformation), relationshipField.getUnderlyingName());
            repository = new ForwardingRelationshipRepository(sourceInformation.getResourceType(), matcher,
                    ForwardingDirection.OWNER, ForwardingDirection.OWNER);
        } else if (behavior == RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER) {
            LOGGER.debug("{}.{}: setting up opposite/owner forwarding repository", toShortName(sourceInformation), relationshipField.getUnderlyingName());
            repository = new ForwardingRelationshipRepository(sourceInformation.getResourceType(), matcher, ForwardingDirection.OPPOSITE, ForwardingDirection.OWNER);
        } else {
            LOGGER.debug("{}.{}: setting up opposite/opposite forwarding repository", toShortName(sourceInformation), relationshipField.getUnderlyingName());
            PreconditionUtil.verifyEquals(RelationshipRepositoryBehavior.FORWARD_OPPOSITE, behavior, "unknown behavior for field=%s", relationshipField);
            repository = new ForwardingRelationshipRepository(sourceInformation.getResourceType(), matcher,
                    ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        }
        repository.setResourceRegistry(moduleRegistry.getResourceRegistry());
        repository.setHttpRequestContextProvider(moduleRegistry.getHttpRequestContextProvider());
        return new MatchedRelationship(relationshipField, implicitRepoInformation, repository);
    }

    private Object toShortName(ResourceInformation information) {
        Class<?> resourceClass = information.getResourceClass();
        return resourceClass != Resource.class ? resourceClass.getName() : information.getResourceType();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object decorateRepository(Object repository) {
        if (repository instanceof ResourceRegistryAware) {
            ((ResourceRegistryAware) repository).setResourceRegistry(moduleRegistry.getResourceRegistry());
        }

        Object decoratedRepository = repository;
        List<RepositoryDecoratorFactory> repositoryDecorators = moduleRegistry.getRepositoryDecoratorFactories();
        for (RepositoryDecoratorFactory repositoryDecorator : repositoryDecorators) {
            decoratedRepository = repositoryDecorator.decorateRepository(decoratedRepository);

            if (decoratedRepository instanceof ResourceRegistryAware) {
                ((ResourceRegistryAware) decoratedRepository).setResourceRegistry(moduleRegistry.getResourceRegistry());
            }
        }

        return decoratedRepository;
    }


    class MatchedRelationship {

        private final ResourceField relationshipField;

        private final RelationshipRepositoryInformation relationshipRepositoryInformation;

        private final Object relRepository;

        public MatchedRelationship(final ResourceField relationshipField,
                                   RelationshipRepositoryInformation relationshipRepositoryInformation, Object relRepository) {
            this.relationshipField = relationshipField;
            this.relationshipRepositoryInformation = relationshipRepositoryInformation;
            this.relRepository = relRepository;
        }

        @Override
        public String toString() {
            return relRepository.toString();
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private ResponseRelationshipEntry getLegacyEntry() {

            final Object decoratedRepository = decorateRepository(relRepository);
            RepositoryInstanceBuilder<Object> relationshipInstanceBuilder =
                    new RepositoryInstanceBuilder<Object>(null, (Class) relRepository.getClass()) {

                        @Override
                        public Object buildRepository() {
                            return decoratedRepository;
                        }
                    };


            return new DirectResponseRelationshipEntry(relationshipInstanceBuilder);
        }

        private RelationshipRepositoryAdapter getAdapter() {
            final Object decoratedRepository = decorateRepository(relRepository);
            List<RepositoryAdapterFactory> adapterFactories = moduleRegistry.getRepositoryAdapterFactories();
            RelationshipRepositoryAdapter adapter = null;
            for (RepositoryAdapterFactory adapterFactory : adapterFactories) {
                if (adapterFactory.accepts(decoratedRepository)) {
                    adapter = adapterFactory
                            .createRelationshipRepositoryAdapter(relationshipField, relationshipRepositoryInformation,
                                    decoratedRepository);
                    break;
                }
            }
            if (adapter == null) {
                throw new IllegalStateException("no RepositoryAdapterFactory found for " + decoratedRepository
                        + ", make sure it is a valid repository, e.g. by implementing ResourceRepository");
            }
            for (RepositoryAdapterFactory adapterFactory : adapterFactories) {
                adapter = adapterFactory.decorate(adapter);
            }
            return adapter;
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private ResourceRepositoryAdapter buildResourceRepositoryAdapter(ResourceInformation resourceInformation) {
        if (resourceRepository == null) {
            return null;
        }
        resourceRepository.information().setResourceInformation(resourceInformation);
        ResourceRepositoryInformation repositoryInformation = resourceRepository.information().build();

        Object instance = resourceRepository.instance;
        final Object decoratedRepository = decorateRepository(instance);
        List<RepositoryAdapterFactory> adapterFactories = moduleRegistry.getRepositoryAdapterFactories();
        ResourceRepositoryAdapter adapter = null;
        for (RepositoryAdapterFactory adapterFactory : adapterFactories) {
            if (adapterFactory.accepts(decoratedRepository)) {
                adapter = adapterFactory.createResourceRepositoryAdapter(repositoryInformation, decoratedRepository);
                break;
            }
        }
        if (adapter == null) {
            throw new IllegalStateException("no RepositoryAdapterFactory found for " + decoratedRepository
                    + ", make sure it is a valid repository, e.g. by implementing ResourceRepository");
        }
        for (RepositoryAdapterFactory adapterFactory : adapterFactories) {
            adapter = adapterFactory.decorate(adapter);
        }
        return adapter;
    }
}
