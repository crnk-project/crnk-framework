package io.crnk.test.mock.reactive;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.repository.ProjectRepository;
import reactor.core.publisher.Mono;

public class ReactiveProjectRepository extends InMemoryReactiveResourceRepository<Project, Long> {

	public ReactiveProjectRepository() {
		super(Project.class);
	}

	@Override
	public Mono<ResourceList<Project>> findAll(QuerySpec querySpec) {
		return Mono.fromCallable(() -> {
			ProjectRepository.ProjectsLinksInformation links = new ProjectRepository.ProjectsLinksInformation();
			links.setLinkValue("testLink");

			ProjectRepository.ProjectsMetaInformation meta = new ProjectRepository.ProjectsMetaInformation();
			meta.setMetaValue("testMeta");

			DefaultResourceList<Project> list = new DefaultResourceList<>();
			list.setMeta(meta);
			list.setLinks(links);
			querySpec.apply(resources.values(), list);

			for (Project project : list) {
				project.getLinks().setValue("someLinkValue");
				project.getMeta().setValue("someMetaValue");
			}

			return list;
		});
	}

}
