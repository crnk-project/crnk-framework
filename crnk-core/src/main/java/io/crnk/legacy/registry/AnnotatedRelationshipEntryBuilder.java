package io.crnk.legacy.registry;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.internal.AnnotatedRelationshipRepositoryAdapter;
import io.crnk.legacy.internal.ParametersFactory;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;

public class AnnotatedRelationshipEntryBuilder implements ResponseRelationshipEntry {

	private RepositoryInstanceBuilder repositoryInstanceBuilder;
	private ModuleRegistry moduleRegistry;

	public AnnotatedRelationshipEntryBuilder(ModuleRegistry moduleRegistry, RepositoryInstanceBuilder repositoryInstanceBuilder) {
		this.moduleRegistry = moduleRegistry;
		this.repositoryInstanceBuilder = repositoryInstanceBuilder;
	}

	@Override
	public Class<?> getTargetAffiliation() {
		@SuppressWarnings("unchecked")
		final Optional<JsonApiRelationshipRepository> annotation = ClassUtils.getAnnotation(
				repositoryInstanceBuilder.getRepositoryClass(),
				JsonApiRelationshipRepository.class
		);

		if (annotation.isPresent()) {
			return annotation.get().target();
		} else {
			throw new IllegalArgumentException(
					String.format(
							"Class %s must be annotated with @JsonApiRelationshipRepository",
							repositoryInstanceBuilder.getClass().getName()
					)
			);
		}
	}

	public AnnotatedRelationshipRepositoryAdapter build(RepositoryMethodParameterProvider parameterProvider) {
		return new AnnotatedRelationshipRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
				new ParametersFactory(parameterProvider));
	}

	@Override
	public String toString() {
		return "AnnotatedRelationshipEntryBuilder{" +
				"repositoryInstanceBuilder=" + repositoryInstanceBuilder +
				'}';
	}
}
