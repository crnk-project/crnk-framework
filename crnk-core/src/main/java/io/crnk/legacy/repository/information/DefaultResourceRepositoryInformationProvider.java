package io.crnk.legacy.repository.information;

import io.crnk.core.engine.information.repository.*;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.repository.UntypedResourceRepository;
import io.crnk.core.repository.decorate.Wrapper;
import io.crnk.core.resource.annotations.JsonApiExposed;
import net.jodah.typetools.TypeResolver;

import java.lang.reflect.Method;
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
		boolean interfaceRepo = ResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean untypedRepo = UntypedResourceRepository.class.isAssignableFrom(repositoryClass);
		return interfaceRepo && !untypedRepo;
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
				getAccess(repository, repositoryClass), exposed);
	}

	protected RepositoryMethodAccess getAccess(Object repository, Class repositoryClass) {
		if (ReadOnlyResourceRepositoryBase.class.isAssignableFrom(repositoryClass)) {
			return new RepositoryMethodAccess(false, false, true, false);
		}

		if (ResourceRepositoryBase.class.isAssignableFrom(repositoryClass)) {
			boolean postable = false;
			boolean patchable = false;
			boolean deletable = false;
			boolean readable = false;
			for (Method method : repositoryClass.getMethods()) {
				if (method.getDeclaringClass() != ResourceRepositoryBase.class) {
					String name = method.getName();
					if (name.startsWith("find")) {
						readable = true;
					} else if (name.startsWith("create")) {
						postable = true;
					} else if (name.startsWith("save")) {
						patchable = true;
					} else if (name.equals("delete")) {
						deletable = true;
					}
				}
			}
			return new RepositoryMethodAccess(postable, patchable, readable, deletable);
		}


		return new RepositoryMethodAccess(true, true, true, true);
	}

	protected Map<String, RepositoryAction> buildActions(Class<? extends Object> repositoryClass) {
		return new HashMap<>();
	}

	protected String getPath(ResourceInformation resourceInformation, Object repository) { // NOSONAR contract ok
		return resourceInformation.getResourcePath();
	}

	protected boolean isExposed(ResourceInformation resourceInformation, Object repository) {
		Object unwrappedRepository = repository;
		while (unwrappedRepository instanceof Wrapper) {
			// allow a wrapper to override the default expose behavior
			JsonApiExposed annotation = unwrappedRepository.getClass().getAnnotation(JsonApiExposed.class);
			if (annotation != null) {
				return annotation.value();
			}

			unwrappedRepository = ((Wrapper) unwrappedRepository).getWrappedObject();
		}
		JsonApiExposed annotation = unwrappedRepository.getClass().getAnnotation(JsonApiExposed.class);
		return annotation == null || annotation.value();
	}


	protected Class<?> getResourceClass(Object repository, Class<?> repositoryClass) {
		if (repository != null) {
			ResourceRepository<?, ?> querySpecRepo = (ResourceRepository<?, ?>) repository;
			Class<?> resourceClass = querySpecRepo.getResourceClass();
			PreconditionUtil.verify(resourceClass != null, "().getResourceClass() must not return null", querySpecRepo);
			return resourceClass;
		}
		if (ResourceRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, repositoryClass);
			return typeArgs[0];
		}
		throw new IllegalStateException("failed to get resource class from " + repositoryClass + ", does it implement ResourceRepository?");
	}
}
