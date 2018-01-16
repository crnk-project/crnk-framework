package io.crnk.gen.typescript;

import groovy.lang.Closure;
import io.crnk.gen.typescript.internal.TSRuntimeExtension;
import org.gradle.api.Project;

public class TSGeneratorExtension extends TSGeneratorConfig {

	private Project project;

	private Runnable initMethod;


	public TSGeneratorExtension(Project project, Runnable initMethod) {
		this.project = project;
		this.initMethod = initMethod;
		this.runtime = new TSRuntimeExtension(project);

		setForked(true);

		setBuildDir(project.getBuildDir());
	}

	public void init() {
		initMethod.run();
	}

	public TSRuntimeConfiguration runtime(Closure closure) {
		return (TSRuntimeConfiguration) project.configure(getRuntime(), closure);
	}

	public TSNpmConfiguration npm(Closure closure) {
		return (TSNpmConfiguration) project.configure(getNpm(), closure);
	}
}
