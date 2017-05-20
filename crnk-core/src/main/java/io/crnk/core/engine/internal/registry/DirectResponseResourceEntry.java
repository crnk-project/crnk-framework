package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.ResourceRepository;

public class DirectResponseResourceEntry implements ResourceEntry {
	private final RepositoryInstanceBuilder<ResourceRepository> repositoryInstanceBuilder;

	public DirectResponseResourceEntry(RepositoryInstanceBuilder<ResourceRepository> repositoryInstanceBuilder) {
		this.repositoryInstanceBuilder = repositoryInstanceBuilder;
	}

	public Object getResourceRepository() {
		return repositoryInstanceBuilder.buildRepository();
	}

	@Override
	public String toString() {
		return "DirectResponseResourceEntry{" +
				"repositoryInstanceBuilder=" + repositoryInstanceBuilder +
				'}';
	}
}
