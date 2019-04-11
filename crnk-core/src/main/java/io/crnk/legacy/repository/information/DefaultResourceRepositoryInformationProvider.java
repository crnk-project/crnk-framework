package io.crnk.legacy.repository.information;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.UntypedResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.legacy.repository.LegacyResourceRepository;
import net.jodah.typetools.TypeResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultResourceRepositoryInformationProvider implements RepositoryInformationProvider {

	@Override
	public boolean accept(Object repository) {
		Objects.requireNonNull(repository);
		Class<? extends Object> repositoryClass = repository.getClass();
		return accept(repositoryClass);
	}

	@Override
	public boolean accept(Class<?> repositoryClass) {
		boolean legacyRepo = LegacyResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean interfaceRepo = ResourceRepositoryV2.class.isAssignableFrom(repositoryClass);
		boolean untypedRepo = UntypedResourceRepository.class.isAssignableFrom(repositoryClass);
		return (legacyRepo || interfaceRepo) && !untypedRepo;
	}

	@Override
	public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationProviderContext context) {
		return build(null, repositoryClass, context);
	}

	@Override
	public RepositoryInformation build(Object repository, RepositoryInformationProviderContext context) {
		return build(repository, repository.getClass(), context);
	}

	private RepositoryInformation build(Object repository, Class<? extends Object> repositoryClass,
										RepositoryInformationProviderContext context) {
		Class<?> resourceClass = getResourceClass(repository, repositoryClass);

		ResourceInformationProvider resourceInformationProvider = context.getResourceInformationBuilder();
		PreconditionUtil.verify(resourceInformationProvider.accept(resourceClass),
				"cannot get resource information for resource class '%s' to setup repository '%s', not annotated with "
						+ "@JsonApiResource?",
				resourceClass, repository);

		ResourceInformation resourceInformation = resourceInformationProvider.build(resourceClass);
		String path = getPath(resourceInformation, repository);
		boolean exposed = repository != null && isExposed(resourceInformation, repository);
		return new ResourceRepositoryInformationImpl(path, resourceInformation, buildActions(repositoryClass),
				getAccess(repository), exposed);
	}

	// FIXME
	protected RepositoryMethodAccess getAccess(Object repository) {
		return new RepositoryMethodAccess(true, true, true, true);
	}

	protected Map<String, RepositoryAction> buildActions(Class<? extends Object> repositoryClass) {
		return new HashMap<>();
	}

	protected String getPath(ResourceInformation resourceInformation, Object repository) { // NOSONAR contract ok
		return resourceInformation.getResourceType();
	}

	protected boolean isExposed(ResourceInformation resourceInformation, Object repository) {
		JsonApiExposed annotation = repository.getClass().getAnnotation(JsonApiExposed.class);
		return annotation == null || annotation.value();
	}


	protected Class<?> getResourceClass(Object repository, Class<?> repositoryClass) {
		if (repository instanceof LegacyResourceRepository) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(LegacyResourceRepository.class, repository.getClass());
			return typeArgs[0];
		} else if (repository != null) {
			ResourceRepositoryV2<?, ?> querySpecRepo = (ResourceRepositoryV2<?, ?>) repository;
			Class<?> resourceClass = querySpecRepo.getResourceClass();
			PreconditionUtil.verify(resourceClass != null, "().getResourceClass() must not return null", querySpecRepo);
			return resourceClass;
		} else if (LegacyResourceRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(LegacyResourceRepository.class, repositoryClass);
			return typeArgs[0];
		} else if (ResourceRepositoryV2.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepositoryV2.class, repositoryClass);
			return typeArgs[0];
		}
		throw new IllegalStateException("failed to get resource class from " + repositoryClass + ", does it implement ResourceRepositoryV2?");
	}
}
