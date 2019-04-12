package io.crnk.example.springboot.microservice.task;

import io.crnk.client.CrnkClient;
import io.crnk.core.module.Module;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.WrappedResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.example.springboot.microservice.MicroServiceApplication;
import io.crnk.example.springboot.microservice.project.Project;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class TaskProjectLinkerModule implements Module {

	@Override
	public String getModuleName() {
		return "microserviceLink";
	}

	@Override
	public void setupModule(ModuleContext context) {
		CrnkClient client = new CrnkClient("http://127.0.0.1:" + MicroServiceApplication.PROJECT_PORT);
		ResourceRepository<Project, Serializable> remoteProjectRepository = client.getRepositoryForType(Project.class);

		// for Task we make use of @JsonApiRelationId and as such directly register the remote resource repository
		// other use cases may also need remote relationship repositories.
		context.addRepository(new MicroServiceRepository(remoteProjectRepository));
	}

	@JsonApiExposed(false)
	class MicroServiceRepository<T, I > extends WrappedResourceRepository<T, I> {

		public MicroServiceRepository(ResourceRepository<T, I> remoteRepository) {
			super();
			setWrappedRepository(remoteRepository);
		}
	}
}
