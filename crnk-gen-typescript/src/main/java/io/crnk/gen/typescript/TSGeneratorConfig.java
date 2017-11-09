package io.crnk.gen.typescript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.gen.typescript.processor.TSEmptyObjectFactoryProcessor;
import io.crnk.gen.typescript.processor.TSExpressionObjectProcessor;
import io.crnk.gen.typescript.processor.TSImportProcessor;
import io.crnk.gen.typescript.processor.TSIndexFileProcessor;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.writer.TSCodeStyle;

public class TSGeneratorConfig {

	private TSCodeStyle codeStyle = new TSCodeStyle();

	protected List<String> metaTransformationClassNames = new ArrayList<>();

	private String metaResolverClassName = "io.crnk.gen.runtime.cdi.CdiMetaResolver";

	private boolean generateExpressions = false;

	private Set<String> includes = new HashSet<>();

	private Set<String> excludes = new HashSet<>();

	private boolean forked = false;

	private TSNpmConfiguration npm = new TSNpmConfiguration();

	private TSRuntimeConfiguration runtime = new TSRuntimeConfiguration();

	private String sourceDirectoryName = "src";

	private File genDir = null;

	private File buildDir = null;

	@JsonIgnore
	private List<TSSourceProcessor> sourceProcessors = new ArrayList<>();

	public TSGeneratorConfig() {
		sourceProcessors.add(new TSExpressionObjectProcessor());
		sourceProcessors.add(new TSImportProcessor());
		sourceProcessors.add(new TSIndexFileProcessor());
		sourceProcessors.add(new TSEmptyObjectFactoryProcessor());
	}

	/**
	 * @return location where the generated sources are placed.
	 */
	public File getGenDir() {
		if (genDir == null) {
			return new File(buildDir, "generated/source/typescript/");
		}
		return genDir;
	}

	public void setGenDir(File genDir) {
		this.genDir = genDir;
	}

	public TSRuntimeConfiguration getRuntime() {
		return runtime;
	}


	public TSNpmConfiguration getNpm() {
		return npm;
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

	boolean isForked() {
		return forked;
	}

	public void setForked(boolean forked) {
		this.forked = forked;
	}

	public File getBuildDir() {
		return buildDir;
	}

	public void setBuildDir(File buildDir) {
		this.buildDir = buildDir;
	}
}
