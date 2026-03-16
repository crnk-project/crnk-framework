package io.crnk.servlet.resource.model;

import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.InMemoryResourceRepository;

public class ServletTestModule extends SimpleModule {
    public ServletTestModule() {
        super("test");

        InMemoryResourceRepository<Task, Object> tasks = new InMemoryResourceRepository<>(Task.class);
        addRepository(tasks);
        addRepository(new InMemoryResourceRepository<>(Locale.class));
        addRepository(new InMemoryResourceRepository<>(Node.class));
        addRepository(new InMemoryResourceRepository<>(NodeComment.class));
        addRepository(new InMemoryResourceRepository<>(Project.class));

        tasks.getMap().put(1L, new Task(1L, "First task"));
    }
}
