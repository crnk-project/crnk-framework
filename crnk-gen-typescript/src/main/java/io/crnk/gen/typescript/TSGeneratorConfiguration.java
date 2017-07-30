package io.crnk.gen.typescript;

import groovy.lang.Closure;
import io.crnk.gen.typescript.model.libraries.ExpressionLibrary;
import io.crnk.gen.typescript.processor.*;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import io.crnk.gen.typescript.transform.TSMetaEnumTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaPrimitiveTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaResourceRepositoryTransformation;
import io.crnk.gen.typescript.writer.TSCodeStyle;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TSGeneratorConfiguration {


	private final Project project;

	private TSCodeStyle codeStyle = new TSCodeStyle();

	private List<TSSourceProcessor> sourceProcessors = new ArrayList<>();

	private List<String> metaTransformationClassNames = new ArrayList<>();

	private String metaResolverClassName = "io.crnk.gen.runtime.deltaspike.DeltaspikeMetaResolver";

	private boolean generateExpressions = false;

	private String sourceDirectoryName = "src";

	private Set<String> includes = new HashSet<>();

	private Set<String> excludes = new HashSet<>();

	// TODO setup crnk frontend library project
	private String expressionLibrary;

	private TSNpmConfiguration npm = new TSNpmConfiguration();

	public TSGeneratorConfiguration(Project project) {
		this.project = project;

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
	 * TODO internal use only, crnk-ngrx frontend library needs to be setup
	 */
	@Deprecated
	public String getExpressionLibrary() {
		return expressionLibrary;
	}

	public void setExpressionLibrary(String expressionLibrary) {
		this.expressionLibrary = expressionLibrary;

		ExpressionLibrary.EXPRESSION_SOURCE.setNpmPackage(expressionLibrary + "/binding/expression");
		ExpressionLibrary.JSONAPI_SOURCE.setNpmPackage(expressionLibrary + "/binding/jsonapi");
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

	public String getSourceDirectoryName() {
		return sourceDirectoryName;
	}

	public void setSourceDirectoryName(String sourceDirectoryName) {
		this.sourceDirectoryName = sourceDirectoryName;
	}
}
