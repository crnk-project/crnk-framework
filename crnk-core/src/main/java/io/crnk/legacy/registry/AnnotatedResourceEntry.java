package io.crnk.legacy.registry;

import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.legacy.internal.AnnotatedResourceRepositoryAdapter;
import io.crnk.legacy.internal.ParametersFactory;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

public class AnnotatedResourceEntry implements ResourceEntry {
	private final RepositoryInstanceBuilder repositoryInstanceBuilder;

	public AnnotatedResourceEntry(RepositoryInstanceBuilder RepositoryInstanceBuilder) {
		this.repositoryInstanceBuilder = RepositoryInstanceBuilder;
	}

	public AnnotatedResourceRepositoryAdapter build(RepositoryMethodParameterProvider parameterProvider) {
		return new AnnotatedResourceRepositoryAdapter<>(repositoryInstanceBuilder.buildRepository(),
				new ParametersFactory(parameterProvider));
	}

	@Override
	public String toString() {
		return "AnnotatedResourceEntryBuilder{" +
				"repositoryInstanceBuilder=" + repositoryInstanceBuilder +
				'}';
	}
}
