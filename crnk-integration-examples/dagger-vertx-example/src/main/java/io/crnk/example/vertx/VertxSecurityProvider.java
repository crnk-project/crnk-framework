package io.crnk.example.vertx;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;
import io.crnk.setup.vertx.CrnkRequestInterceptor;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.reactivex.ext.auth.User;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

public class VertxSecurityProvider implements SecurityProvider, CrnkRequestInterceptor {

    private static final String USER_CONTEXT_ATTRIBUTE = "user";

    private final AuthProvider authProvider;

    public VertxSecurityProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }


    @Override
    public boolean isUserInRole(String role, SecurityProviderContext context) {
        QueryContext queryContext = context.getQueryContext();
        return queryContext.getAttribute(USER_CONTEXT_ATTRIBUTE) != null;
    }

    @Override
    public boolean isAuthenticated(SecurityProviderContext context) {
        QueryContext queryContext = context.getQueryContext();
        return queryContext.getAttribute(USER_CONTEXT_ATTRIBUTE) != null;
    }

    @Override
    public Mono<HttpRequestContext> onRequest(Mono<HttpRequestContext> mono) {
        return mono.flatMap(request -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("name", "john doe");
            io.vertx.reactivex.ext.auth.AuthProvider rxAuthProvider = io.vertx.reactivex.ext.auth.AuthProvider.newInstance(authProvider);
            Mono<User> userSingle = RxJava2Adapter.singleToMono(rxAuthProvider.rxAuthenticate(jsonObject));
            return userSingle.doOnNext(user -> request.getQueryContext().setAttribute(USER_CONTEXT_ATTRIBUTE, user)).map(user -> request);
        });
    }
}
