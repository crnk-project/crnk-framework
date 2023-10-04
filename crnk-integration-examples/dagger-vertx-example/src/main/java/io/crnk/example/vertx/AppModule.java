package io.crnk.example.vertx;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.example.vertx.repository.ProjectRepository;
import io.crnk.home.HomeModule;
import io.crnk.security.ResourcePermission;
import io.crnk.security.SecurityConfig;
import io.crnk.security.SecurityModule;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import jakarta.inject.Singleton;

@Module
public class AppModule {

    @Provides
    @Singleton
    @IntoSet
    public ResourceRepository projectRepository() {
        return new ProjectRepository();
    }

    @Provides
    @Singleton
    @IntoSet
    public io.crnk.core.module.Module homeModule() {
        return HomeModule.create();
    }

    @Provides
    @Singleton
    @IntoSet
    public io.crnk.core.module.Module securityModule() {
        SecurityConfig.Builder builder = SecurityConfig.builder();
        builder.permitRole("test", ResourcePermission.ALL);
        return SecurityModule.newServerModule(builder.build());
    }

    @Provides
    @Singleton
    public VertxSecurityProvider securityProvider(AuthProvider authProvider) {
        return new VertxSecurityProvider(authProvider);
    }

    @Provides
    @Singleton
    public AuthProvider authProvider() {
        return (authInfo, resultHandler) -> {
            User user = User.create(new JsonObject().put("name", "john doe"));

            resultHandler.handle(Future.succeededFuture(user));
        };
    }
}