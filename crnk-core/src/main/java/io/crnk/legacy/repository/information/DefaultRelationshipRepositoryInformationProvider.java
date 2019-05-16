package io.crnk.legacy.repository.information;

import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.MatchedRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.UntypedRelationshipRepository;
import io.crnk.legacy.repository.LegacyRelationshipRepository;
import net.jodah.typetools.TypeResolver;

public class DefaultRelationshipRepositoryInformationProvider implements RepositoryInformationProvider {

    @Override
    public boolean accept(Object repository) {
        Class<? extends Object> repositoryClass = repository.getClass();
        return accept(repositoryClass);
    }

    @Override
    public boolean accept(Class<?> repositoryClass) {
        return !UntypedRelationshipRepository.class.isAssignableFrom(repositoryClass) && (
                LegacyRelationshipRepository.class.isAssignableFrom(repositoryClass) || OneRelationshipRepository.class
                        .isAssignableFrom(repositoryClass) || ManyRelationshipRepository.class.isAssignableFrom(repositoryClass));
    }

    @Override
    public RepositoryInformation build(Object repository, RepositoryInformationProviderContext context) {
        return buildInformation(repository, repository.getClass(), context);
    }

    @Override
    public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationProviderContext context) {
        return buildInformation(null, repositoryClass, context);
    }

    private RepositoryInformation buildInformation(Object repository, Class<? extends Object> repositoryClass,
                                                   RepositoryInformationProviderContext context) {
        RelationshipMatcher matcher;
        if (repository instanceof MatchedRelationshipRepository) {
            matcher = ((MatchedRelationshipRepository) repository).getMatcher();
        } else {
            Class<?> sourceResourceClass = getSourceResourceClass(repository, repositoryClass);
            Class<?> targetResourceClass = getTargetResourceClass(repository, repositoryClass);

            PreconditionUtil.assertNotNull("no sourceResourceClass", sourceResourceClass);
            PreconditionUtil.assertNotNull("no targetResourceClass", targetResourceClass);

            String sourceResourceType = context.getResourceInformationBuilder().getResourceType(sourceResourceClass);
            String targetResourceType = context.getResourceInformationBuilder().getResourceType(targetResourceClass);
            matcher = new RelationshipMatcher();
            matcher.rule().source(sourceResourceType).target(targetResourceType).add();
        }

        RepositoryMethodAccess access = getAccess(repository);
        return new RelationshipRepositoryInformationImpl(matcher, access);
    }

    // FIXME
    protected RepositoryMethodAccess getAccess(Object repository) {
        return new RepositoryMethodAccess(true, true, true, true);
    }

    public Class<?> getSourceResourceClass(Object repository, Class<?> repositoryClass) {
        if (LegacyRelationshipRepository.class.isAssignableFrom(repositoryClass)) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(LegacyRelationshipRepository.class, repositoryClass);
            return typeArgs[0];
        } else if (repository != null) {
            RelationshipRepository<?, ?, ?, ?> querySpecRepo = (RelationshipRepository<?, ?, ?, ?>) repository;
            return querySpecRepo.getSourceResourceClass();
        } else {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
            return typeArgs[0];
        }
    }

    protected Class<?> getTargetResourceClass(Object repository, Class<?> repositoryClass) {
        if (LegacyRelationshipRepository.class.isAssignableFrom(repositoryClass)) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(LegacyRelationshipRepository.class, repositoryClass);
            return typeArgs[2];
        } else if (repository != null) {
            RelationshipRepository<?, ?, ?, ?> querySpecRepo = (RelationshipRepository<?, ?, ?, ?>) repository;
            return querySpecRepo.getTargetResourceClass();
        } else {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
            return typeArgs[2];
        }
    }
}
