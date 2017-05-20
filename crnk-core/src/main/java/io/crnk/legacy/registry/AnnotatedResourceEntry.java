package io.crnk.legacy.registry;

import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.AnnotatedResourceRepositoryAdapter;
import io.crnk.legacy.internal.ParametersFactory;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;

public class AnnotatedResourceEntry<T, ID extends Serializable> implements ResourceEntry {
	private final RepositoryInstanceBuilder repositoryInstanceBuilder;

	@Deprecated
	private ModuleRegistry moduleRegistry;

	public AnnotatedResourceEntry(ModuleRegistry moduleRegistry, RepositoryInstanceBuilder RepositoryInstanceBuilder) {
		this.moduleRegistry = moduleRegistry;
		this.repositoryInstanceBuilder = RepositoryInstanceBuilder;
	}

	public AnnotatedResourceRepositoryAdapter build(RepositoryMethodParameterProvider parameterProvider) {
		return new AnnotatedResourceRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
				new ParametersFactory(moduleRegistry, parameterProvider));
	}

	@Override
	public String toString() {
		return "AnnotatedResourceEntryBuilder{" +
				"repositoryInstanceBuilder=" + repositoryInstanceBuilder +
				'}';
	}
}
