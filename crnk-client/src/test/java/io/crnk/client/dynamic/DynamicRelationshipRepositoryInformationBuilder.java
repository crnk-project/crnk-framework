package io.crnk.client.dynamic;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilderContext;
import io.crnk.core.engine.information.resource.ResourceInformation;

// tag::docs[]
public class DynamicRelationshipRepositoryInformationBuilder implements RepositoryInformationBuilder {

	@Override
	public boolean accept(Class<?> repositoryClass) {
		return false;
	}

	@Override
	public boolean accept(Object repository) {
		return repository instanceof DynamicRelationshipRepository;
	}

	@Override
	public RepositoryInformation build(Object repositoryObj, RepositoryInformationBuilderContext context) {
		DynamicResourceRepositoryInformationBuilder resourceBuilder = new DynamicResourceRepositoryInformationBuilder();
		ResourceInformation information = resourceBuilder.build((Object) null, context).getResourceInformation();
		InformationBuilder builder = context.builder();
		InformationBuilder.RelationshipRepository repositoryBuilder = builder.createRelationshipRepository(information, information);
		return repositoryBuilder.build();
	}

	@Override
	public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
		throw new UnsupportedOperationException();
	}
}
// end::docs[]