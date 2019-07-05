package io.crnk.data.facet.setup;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.module.SimpleModule;
import io.crnk.data.facet.FacetModule;
import io.crnk.data.facet.FacetModuleConfig;
import io.crnk.data.facet.FacetResource;
import io.crnk.data.facet.internal.FacetRepositoryImpl;

public class FacetTestSetup {

	private final FacetedProjectRepository projectRepository;

	private CrnkBoot boot;

	private FacetRepositoryImpl repository;

	public FacetTestSetup() {
		FacetModuleConfig config = new FacetModuleConfig();
		FacetModule facetModule = new FacetModule(config);

		SimpleModule testModule = new SimpleModule("test");
		projectRepository = new FacetedProjectRepository();
		testModule.addRepository(projectRepository);
		testModule.addRepository(new FacetedTaskRepository()); // => not exposed

		boot = new CrnkBoot();
		boot.addModule(testModule);
		boot.addModule(facetModule);

	}

	public void boot() {
		boot.boot();

		RegistryEntry entry = boot.getResourceRegistry().getEntry(FacetResource.class);
		repository = (FacetRepositoryImpl) entry.getResourceRepository().getImplementation();

		for (int i = 0; i < 16; i++) {
			String name = "project" + (int) Math.sqrt(i);
			FacetedProject project = new FacetedProject();
			project.setId((long) i);
			project.setName(name);
			project.setPriority(i & 1);
			projectRepository.save(project);
		}
	}

	public CrnkBoot getBoot() {
		return boot;
	}

	public FacetRepositoryImpl getRepository() {
		return repository;
	}
}
