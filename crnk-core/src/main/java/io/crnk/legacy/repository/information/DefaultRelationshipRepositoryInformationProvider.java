package io.crnk.legacy.repository.information;

import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.repository.MatchedRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.UntypedRelationshipRepository;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
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
				RelationshipRepository.class.isAssignableFrom(repositoryClass) || RelationshipRepositoryV2.class
						.isAssignableFrom(repositoryClass)
						|| ClassUtils.getAnnotation(repositoryClass, JsonApiRelationshipRepository.class).isPresent());
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
		}
		else {
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
		Optional<JsonApiRelationshipRepository> annotation =
				ClassUtils.getAnnotation(repositoryClass, JsonApiRelationshipRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().source();
		}
		else if (RelationshipRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
			return typeArgs[0];
		}
		else if (repository != null) {
			RelationshipRepositoryV2<?, ?, ?, ?> querySpecRepo = (RelationshipRepositoryV2<?, ?, ?, ?>) repository;
			return querySpecRepo.getSourceResourceClass();
		}
		else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepositoryV2.class, repositoryClass);
			return typeArgs[0];
		}
	}

	protected Class<?> getTargetResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiRelationshipRepository> annotation =
				ClassUtils.getAnnotation(repositoryClass, JsonApiRelationshipRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().target();
		}
		else if (RelationshipRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
			return typeArgs[2];
		}
		else if (repository != null) {
			RelationshipRepositoryV2<?, ?, ?, ?> querySpecRepo = (RelationshipRepositoryV2<?, ?, ?, ?>) repository;
			return querySpecRepo.getTargetResourceClass();
		}
		else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepositoryV2.class, repositoryClass);
			return typeArgs[2];
		}
	}
}
