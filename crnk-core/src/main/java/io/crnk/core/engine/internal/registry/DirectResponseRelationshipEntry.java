package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.RelationshipRepository;
import net.jodah.typetools.TypeResolver;

public class DirectResponseRelationshipEntry implements ResponseRelationshipEntry {

	private static final int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

	private RepositoryInstanceBuilder<RelationshipRepository> repositoryInstanceBuilder;

	public DirectResponseRelationshipEntry(RepositoryInstanceBuilder repositoryInstanceBuilder) {
		this.repositoryInstanceBuilder = repositoryInstanceBuilder;
	}

	@Override
	public Class<?> getTargetAffiliation() {
		Class<?> repoClass = repositoryInstanceBuilder.getRepositoryClass();
		Class<?> repoInterface = RelationshipRepositoryV2.class.isAssignableFrom(repoClass) ? RelationshipRepositoryV2.class
				: RelationshipRepository.class;

		Class<?>[] typeArgs = TypeResolver
				.resolveRawArguments(repoInterface, repoClass);

		return typeArgs[TARGET_TYPE_GENERIC_PARAMETER_IDX];
	}

	public Object getRepositoryInstanceBuilder() {
		return repositoryInstanceBuilder.buildRepository();
	}

	@Override
	public String toString() {
		return "DirectResponseRelationshipEntry{" +
				"repositoryInstanceBuilder=" + repositoryInstanceBuilder +
				'}';
	}
}
