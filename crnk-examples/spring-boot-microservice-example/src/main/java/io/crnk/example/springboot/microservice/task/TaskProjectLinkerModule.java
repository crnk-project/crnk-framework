package io.crnk.example.springboot.microservice.task;

import java.io.Serializable;

import io.crnk.client.CrnkClient;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.microservice.MicroServiceApplication;
import io.crnk.example.springboot.microservice.project.Project;
import org.springframework.stereotype.Component;

@Component
public class TaskProjectLinkerModule implements Module {

	@Override
	public String getModuleName() {
		return "microserviceLink";
	}

	@Override
	public void setupModule(ModuleContext context) {
		CrnkClient client = new CrnkClient("http://127.0.0.1:" + MicroServiceApplication.PROJECT_PORT);
		ResourceRepositoryV2<Project, Serializable> remoteProjectRepository = client.getRepositoryForType(Project.class);
		context.addRepository(new MicroServiceRepository(remoteProjectRepository));
	}

	class MicroServiceRepository<T, I extends Serializable> implements ResourceRepositoryV2<T, I> {

		private final ResourceRepositoryV2<T, I> remoteRepository;

		public MicroServiceRepository(ResourceRepositoryV2<T, I> remoteRepository) {
			super();
			this.remoteRepository = remoteRepository;
		}

		@Override
		public Class<T> getResourceClass() {
			return remoteRepository.getResourceClass();
		}

		@Override
		public T findOne(I id, QuerySpec querySpec) {
			return remoteRepository.findOne(id, querySpec);
		}

		@Override
		public ResourceList<T> findAll(QuerySpec querySpec) {
			return remoteRepository.findAll(querySpec);
		}

		@Override
		public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
			return remoteRepository.findAll(ids, querySpec);
		}

		@Override
		public <S extends T> S save(S resource) {
			return remoteRepository.save(resource);
		}

		@Override
		public <S extends T> S create(S resource) {
			return remoteRepository.create(resource);
		}

		@Override
		public void delete(I id) {
			remoteRepository.delete(id);
		}
	}
}
