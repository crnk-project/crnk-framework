package io.crnk.core.engine.internal.information.repository;

import java.util.Map;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.utils.Optional;

public class ResourceRepositoryInformationImpl implements ResourceRepositoryInformation {

	private ResourceInformation resourceInformation;


	private String path;

	private Map<String, RepositoryAction> actions;

	private RepositoryMethodAccess access;

	private boolean exposed;

	private boolean isVersioned;

	private int version;

	public ResourceRepositoryInformationImpl(String path,
			ResourceInformation resourceInformation, Map<String, RepositoryAction> actions,
			RepositoryMethodAccess access, boolean exposed) {
		this.resourceInformation = resourceInformation;
		this.path = path;
		this.actions = actions;
		this.access = access;
		this.exposed = exposed;
	}

    public ResourceRepositoryInformationImpl(String path,
                                             ResourceInformation resourceInformation, Map<String, RepositoryAction> actions,
                                             RepositoryMethodAccess access, boolean exposed, boolean isVersioned, int version) {
        this(path, resourceInformation, actions, access, exposed);
        this.isVersioned = isVersioned;
        this.version = version;
    }

	@Override
	public Optional<ResourceInformation> getResourceInformation() {
		return Optional.of(getResource());
	}

	@Override
	public ResourceInformation getResource() {
		return resourceInformation;
	}

	@Override
	public String getResourceType() {
		return resourceInformation.getResourceType();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Map<String, RepositoryAction> getActions() {
		return actions;
	}

	@Override
	public boolean isExposed() {
		return exposed;
	}

	@Override
	public RepositoryMethodAccess getAccess() {
		return access;
	}

	@Override
    public boolean isVersioned() {
        return isVersioned;
    }

    @Override
    public int getVersion() {
        return version;
    }
}