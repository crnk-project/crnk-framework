package io.crnk.gen.typescript.internal;

import groovy.lang.Closure;
import io.crnk.gen.runtime.spring.SpringRuntimeConfig;
import io.crnk.gen.typescript.TSRuntimeConfiguration;
import org.gradle.api.Project;

public class TSRuntimeExtension extends TSRuntimeConfiguration {

	private Project project;

	public TSRuntimeExtension(Project project) {
		this.project = project;
	}

	public SpringRuntimeConfig spring(Closure closure) {
		return (SpringRuntimeConfig) project.configure(getSpring(), closure);
	}
}
