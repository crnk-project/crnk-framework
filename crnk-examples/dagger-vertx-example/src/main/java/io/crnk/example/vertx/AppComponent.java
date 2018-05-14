package io.crnk.example.vertx;


import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

	void inject(CrnkVerticle vehicle);

	// void inject(MyFragment fragment);
	// void inject(MyService service);
}