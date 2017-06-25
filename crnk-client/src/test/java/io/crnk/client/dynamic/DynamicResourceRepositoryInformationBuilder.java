package io.crnk.client.dynamic;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilderContext;
import io.crnk.core.engine.information.resource.ResourceFieldType;

import java.util.List;

// tag::docs[]
public class DynamicResourceRepositoryInformationBuilder implements RepositoryInformationBuilder {


	@Override
	public boolean accept(Object repository) {
		return repository instanceof DynamicResourceRepository;
	}

	@Override
	public RepositoryInformation build(Object repositoryObj, RepositoryInformationBuilderContext context) {
		InformationBuilder builder = context.builder();
		InformationBuilder.ResourceRepository repositoryBuilder = builder.createResourceRepository(Resource.class, "dynamic");
		InformationBuilder.Resource resource = repositoryBuilder.resource();

		resource.addField("id", ResourceFieldType.ID, String.class);
		resource.addField("value", ResourceFieldType.ATTRIBUTE, String.class);
		resource.addField("parent", ResourceFieldType.RELATIONSHIP, Resource.class).oppositeResourceType("dynamic");
		resource.addField("children", ResourceFieldType.RELATIONSHIP, List.class).oppositeResourceType("dynamic");

		return repositoryBuilder.build();
	}

	@Override
	public boolean accept(Class<?> repositoryClass) {
		return false;
	}

	@Override
	public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
		throw new UnsupportedOperationException();
	}
}
// end::docs[]