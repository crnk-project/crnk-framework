package io.crnk.legacy.internal;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.LegacyResourceRepository;

public class DirectResponseResourceEntry implements ResourceEntry {

	private final RepositoryInstanceBuilder<LegacyResourceRepository> repositoryInstanceBuilder;

	private ResourceRepositoryInformation information;

	public DirectResponseResourceEntry(RepositoryInstanceBuilder<LegacyResourceRepository> repositoryInstanceBuilder,
									   ResourceRepositoryInformation information) {
		this.repositoryInstanceBuilder = repositoryInstanceBuilder;
		this.information = information;
	}

	public Object getResourceRepository() {
		return repositoryInstanceBuilder.buildRepository();
	}

	@Override
	public String toString() {
		return repositoryInstanceBuilder.buildRepository().toString();
	}

	@Override
	public ResourceRepositoryInformation getRepositoryInformation() {
		return information;
	}
}
