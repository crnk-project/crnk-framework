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

		// reconfigure within extension, not in TSGeneratorConfig since the later is used also in forked mode
		setForked(true);
		setBuildDir(project.getBuildDir());
		getExcludes().add("resources.meta");

		getNpm().setPackageName("@packageNameNotSpecified");
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
