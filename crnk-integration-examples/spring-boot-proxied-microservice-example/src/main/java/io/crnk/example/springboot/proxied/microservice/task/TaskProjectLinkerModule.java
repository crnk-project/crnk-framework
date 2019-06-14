package io.crnk.example.springboot.proxied.microservice.task;

import io.crnk.client.CrnkClient;
import io.crnk.core.module.Module;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.WrappedResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.core.resource.proxy.ProxyResource;
import io.crnk.example.springboot.proxied.microservice.ProxiedMicroServiceApplication;
import io.crnk.example.springboot.proxied.microservice.project.Project;
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
        CrnkClient client = new CrnkClient("http://127.0.0.1:" + ProxiedMicroServiceApplication.PROJECT_PORT);
        ResourceRepository<ProjectProxy, Serializable> remoteProjectRepository = client.getRepositoryForType(ProjectProxy.class);

        // for Task we make use of @JsonApiRelationId and as such directly register the remote resource repository
        // other use cases may also need remote relationship repositories.
        context.addRepository(new ProxiedMicroServiceRepository(remoteProjectRepository));
    }

    @JsonApiExposed(false)
    class ProxiedMicroServiceRepository<T extends ProxyResource, I> extends WrappedResourceRepository<T, I> {

        public ProxiedMicroServiceRepository(ResourceRepository<T, I> remoteRepository) {
            super(remoteRepository);
        }
    }
}
