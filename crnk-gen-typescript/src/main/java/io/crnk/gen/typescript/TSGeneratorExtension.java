package io.crnk.gen.typescript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import groovy.lang.Closure;
import io.crnk.gen.typescript.processor.TSEmptyObjectFactoryProcessor;
import io.crnk.gen.typescript.processor.TSExpressionObjectProcessor;
import io.crnk.gen.typescript.processor.TSImportProcessor;
import io.crnk.gen.typescript.processor.TSIndexFileProcessor;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import io.crnk.gen.typescript.transform.TSMetaEnumTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaPrimitiveTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaResourceRepositoryTransformation;
import io.crnk.gen.typescript.writer.TSCodeStyle;
import org.gradle.api.Project;

public class TSGeneratorExtension {


	private final Project project;

	private final Runnable initMethod;

	private TSCodeStyle codeStyle = new TSCodeStyle();

	private List<TSSourceProcessor> sourceProcessors = new ArrayList<>();

	private List<String> metaTransformationClassNames = new ArrayList<>();

	private String metaResolverClassName = "io.crnk.gen.runtime.deltaspike.DeltaspikeMetaResolver";

	private boolean generateExpressions = false;

	private Set<String> includes = new HashSet<>();

	private Set<String> excludes = new HashSet<>();

	// TODO setup crnk frontend library project
	private String expressionLibrary;

	private TSNpmConfiguration npm = new TSNpmConfiguration();

	private TSRuntimeConfiguration runtime = new TSRuntimeConfiguration();

	private String sourceDirectoryName = "src";

	private File genDir = null;

	public TSGeneratorExtension(Project project, Runnable initMethod) {
		this.project = project;
		this.initMethod = initMethod;

		sourceProcessors.add(new TSExpressionObjectProcessor());
		sourceProcessors.add(new TSImportProcessor());
		sourceProcessors.add(new TSIndexFileProcessor());
		sourceProcessors.add(new TSEmptyObjectFactoryProcessor());

		// classes are loaded by the application class loader
		metaTransformationClassNames.add(TSMetaDataObjectTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaEnumTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaPrimitiveTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaResourceRepositoryTransformation.class.getName());
	}

	public void init() {
		initMethod.run();
	}

	/**
	 * @return location where the generated sources are placed.
	 */
	public File getGenDir() {
		if (genDir == null) {
			return new File(project.getBuildDir(), "generated/source/typescript/");
		}
		return genDir;
	}

	public void setGenDir(File genDir) {
		this.genDir = genDir;
	}

	public TSRuntimeConfiguration getRuntime() {
		return runtime;
	}

	public TSRuntimeConfiguration runtime(Closure closure) {
		return (TSRuntimeConfiguration) project.configure(runtime, closure);
	}

	public TSNpmConfiguration getNpm() {
		return npm;
	}

	public TSNpmConfiguration npm(Closure closure) {
		return (TSNpmConfiguration) project.configure(npm, closure);
	}

	/**
	 * @return list of meta ids (or prefixes there of) that should be generated. An empty list generates everything.
	 */
	public Set<String> getIncludes() {
		return includes;
	}

	public void setIncludes(Set<String> includes) {
		this.includes = includes;
	}

	/**
	 * @return list of meta ids (or prefixes there of) that should not be exported to typescript
	 */
	public Set<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(Set<String> excludes) {
		this.excludes = excludes;
	}

	public boolean getGenerateExpressions() {
		return generateExpressions;
	}

	public void setGenerateExpressions(boolean generateExpressions) {
		this.generateExpressions = generateExpressions;
	}

	/**
	 * @return code style to use for generated TypeScript sources.
	 */
	public TSCodeStyle getCodeStyle() {
		return codeStyle;
	}

	/**
	 * @return TSSourceProcessor instances to use during generation.
	 */
	public List<TSSourceProcessor> getSourceProcessors() {
		List<TSSourceProcessor> enabledSourceProcessors = new ArrayList<>();
		for (TSSourceProcessor sourceProcessor : sourceProcessors) {
			if (generateExpressions || !(sourceProcessor instanceof TSExpressionObjectProcessor)) {
				enabledSourceProcessors.add(sourceProcessor);
			}
		}
		return enabledSourceProcessors;
	}

	/**
	 * @return TSMetaTransformation class names to instantiate and use.
	 */
	public List<String> getMetaTransformationClassNames() {
		return metaTransformationClassNames;
	}


	public String getMetaResolverClassName() {
		return metaResolverClassName;
	}

	public void setMetaResolverClassName(String metaResolverClassName) {
		this.metaResolverClassName = metaResolverClassName;
	}

	/**
	 * Only to be used with packagingEnabled is true.
	 */
	public String getSourceDirectoryName() {
		return sourceDirectoryName;
	}

	public void setSourceDirectoryName(String sourceDirectoryName) {
		this.sourceDirectoryName = sourceDirectoryName;
	}


}
