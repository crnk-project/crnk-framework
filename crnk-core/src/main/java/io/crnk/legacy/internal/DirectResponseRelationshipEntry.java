package io.crnk.legacy.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.RelationshipRepository;

import net.jodah.typetools.TypeResolver;

public class DirectResponseRelationshipEntry implements ResponseRelationshipEntry {

	private static final int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

	private RepositoryInstanceBuilder repositoryInstanceBuilder;

	public DirectResponseRelationshipEntry(RepositoryInstanceBuilder repositoryInstanceBuilder) {
		this.repositoryInstanceBuilder = repositoryInstanceBuilder;
	}

	@Override
	public String getTargetResourceType() {
		Class<?> repoClass = repositoryInstanceBuilder.getRepositoryClass();
		Class<?> repoInterface = RelationshipRepositoryV2.class.isAssignableFrom(repoClass) ? RelationshipRepositoryV2.class
				: RelationshipRepository.class;

		Class<?>[] typeArgs = TypeResolver
				.resolveRawArguments(repoInterface, repoClass);

		Class<?> typeArg = typeArgs[TARGET_TYPE_GENERIC_PARAMETER_IDX];
		Optional<JsonApiResource> annotation = ClassUtils.getAnnotation(typeArg, JsonApiResource.class);
		return annotation.get().type();
	}

	public Object getRepositoryInstanceBuilder() {
		return repositoryInstanceBuilder.buildRepository();
	}

	@Override
	public String toString() {
		return repositoryInstanceBuilder.buildRepository().toString();
	}
}
