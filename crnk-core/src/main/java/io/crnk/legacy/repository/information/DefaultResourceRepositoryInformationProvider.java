package io.crnk.legacy.repository.information;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.UntypedResourceRepository;
import io.crnk.legacy.repository.ResourceRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;

import net.jodah.typetools.TypeResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultResourceRepositoryInformationProvider implements RepositoryInformationProvider {

	@Override
	public boolean accept(Object repository) {
		Class<? extends Object> repositoryClass = repository.getClass();
		return accept(repositoryClass);
	}

	@Override
	public boolean accept(Class<?> repositoryClass) {
		boolean legacyRepo = ResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean interfaceRepo = ResourceRepositoryV2.class.isAssignableFrom(repositoryClass);
		boolean anontationRepo = ClassUtils.getAnnotation(repositoryClass, JsonApiResourceRepository.class).isPresent();
		boolean untypedRepo = UntypedResourceRepository.class.isAssignableFrom(repositoryClass);
		return (legacyRepo || interfaceRepo || anontationRepo) && !untypedRepo;
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
		PreconditionUtil.assertTrue("cannot get ResourceInformation for " + resourceClass,
				resourceInformationProvider.accept(resourceClass));

		ResourceInformation resourceInformation = resourceInformationProvider.build(resourceClass);
		String path = getPath(resourceInformation, repository);

		return new ResourceRepositoryInformationImpl(path, resourceInformation, buildActions(repositoryClass), getAccess(repository));
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

	protected Class<?> getResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiResourceRepository> annotation = ClassUtils.getAnnotation(repositoryClass,
				JsonApiResourceRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().value();
		} else if (repository instanceof ResourceRepository) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, repository.getClass());
			return typeArgs[0];
		} else if (repository != null) {
			ResourceRepositoryV2<?, ?> querySpecRepo = (ResourceRepositoryV2<?, ?>) repository;
			return querySpecRepo.getResourceClass();
		} else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepositoryV2.class, repositoryClass);
			return typeArgs[0];
		}
	}
}
