package io.crnk.gen.typescript;

import groovy.lang.Closure;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import io.crnk.gen.typescript.transform.TSMetaEnumTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaPrimitiveTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaResourceRepositoryTransformation;
import org.gradle.api.Project;

public class TSGeneratorExtension extends TSGeneratorConfig {

	private Project project;

	private Runnable initMethod;


	public TSGeneratorExtension(Project project, Runnable initMethod) {
		this.project = project;
		this.initMethod = initMethod;

		setForked(true);

		setBuildDir(project.getBuildDir());

		// classes are loaded by the application class loader
		metaTransformationClassNames.add(TSMetaDataObjectTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaEnumTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaPrimitiveTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaResourceRepositoryTransformation.class.getName());
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
