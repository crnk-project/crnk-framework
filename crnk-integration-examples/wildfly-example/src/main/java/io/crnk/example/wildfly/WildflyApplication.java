package io.crnk.example.wildfly;

import io.crnk.rs.CrnkFeature;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
public class WildflyApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<>();
		set.add(CrnkFeature.class);
		return set;
	}
}
